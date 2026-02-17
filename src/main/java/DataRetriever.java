import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

   public Dish findDishById(Integer id) {
       DBConnection dbConnection = new DBConnection();
       Dish dish = null;
        String findDishByIdQuery = """
              SELECT id, name, price, dish_type FROM dish WHERE id = ?;
                """;
        try(Connection connection = dbConnection.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(findDishByIdQuery)){
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                dish = new Dish();
                dish.setId(resultSet.getInt("id"));
                dish.setName(resultSet.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getDouble("price"));
                dish.setIngredients(findDishIngredientByDishId(id));

            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return dish;
    }

    public List<Ingredient> findIngredients(int page, int size){
       DBConnection dbConnection = new DBConnection();
       List<Ingredient> ingredients = new ArrayList<>();

       String findIngredientsSql = """
               select ingredient.id , ingredient.name, ingredient.price, ingredient.category from ingredient
               limit ? offset ?
               """;
       int offset = (page - 1) * size;
       try(Connection connection = dbConnection.getConnection();
       PreparedStatement preparedStatement = connection.prepareStatement(findIngredientsSql)){
           preparedStatement.setInt(1,size);
           preparedStatement.setInt(2,offset);
           ResultSet resultSet = preparedStatement.executeQuery();
           while (resultSet.next()) {
               Ingredient ingredient = new Ingredient();
               ingredient.setId(resultSet.getInt("id"));
               ingredient.setName(resultSet.getString("name"));
               ingredient.setPrice(resultSet.getDouble("price"));
               ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
               ingredients.add(ingredient);
           }
       }catch(SQLException e){
           throw new RuntimeException(e);
       }
       return ingredients;
    }

    public List<Dish> findDishsByIngredientName(String IngredientName){
       List<Dish> dishes = new ArrayList<>();
       DBConnection dbConnection = new DBConnection();

       String findDishsByIngredientNameSql = """
        SELECT DISTINCT d.id, d.name, d.price, d.dish_type
        FROM dish d
        JOIN dishingredient di ON di.id_dish = d.id
        JOIN ingredient i ON i.id = di.id_ingredient
        WHERE i.name ILIKE ?
               """;

       try(Connection connection = dbConnection.getConnection();
       PreparedStatement preparedStatement = connection.prepareStatement(findDishsByIngredientNameSql)){
           preparedStatement.setString(1,IngredientName);
           ResultSet resultSet = preparedStatement.executeQuery();
           while (resultSet.next()) {
               Dish dish = new Dish();
               dish.setId(resultSet.getInt("id"));
               dish.setName(resultSet.getString("name"));
               dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
               dish.setPrice(resultSet.getDouble("price"));
               dish.setIngredients(findDishIngredientByDishId(resultSet.getInt("id")));
               dishes.add(dish);
           }
       }catch(SQLException e){
           throw new RuntimeException(e);
       }
       return dishes;
    }

    private List<DishIngredient> findDishIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        List<DishIngredient> listIngredient= new ArrayList<>();
        Dish dish=null;

        String findDishIngredientByDishIdSql = """
                SELECT di.id AS di_id, di.quantity_required, di.unit,
                       i.id AS ingredient_id, i.name AS ingredient_name,
                       i.price AS ingredient_price, i.category AS ingredient_category
                FROM dishingredient di
                JOIN ingredient i ON di.id_ingredient = i.id
                WHERE di.id_dish = ?
                """;

        try(Connection connection= dbConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(findDishIngredientByDishIdSql)){
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("ingredient_id"));
                ingredient.setName(resultSet.getString("ingredient_name"));
                ingredient.setPrice(resultSet.getDouble("ingredient_price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("ingredient_category")));

                DishIngredient dishIngredient = new DishIngredient();
                dishIngredient.setId(resultSet.getInt("di_id"));
                dishIngredient.setDish(dish);
                dishIngredient.setIngredient(ingredient);
                dishIngredient.setQuantity(resultSet.getDouble("quantity_required"));
                dishIngredient.setUnit(Unit.valueOf(resultSet.getString("unit")));
                listIngredient.add(dishIngredient);
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }

        return listIngredient;
    }

    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size
    ) {

       DBConnection dbConnection = new DBConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("""
        SELECT i.id, i.name, i.price, i.category
        FROM ingredient i
        """);

        List<Object> params = new ArrayList<>();

        if (dishName != null && !dishName.isBlank()) {
            sql.append("""
            JOIN dishingredient di ON di.id_ingredient = i.id
            JOIN dish d ON d.id = di.id_dish
        """);
        }

        sql.append(" WHERE 1=1 ");

        if (ingredientName != null && !ingredientName.isBlank()) {
            sql.append(" AND i.name ILIKE ?");
            params.add("%" + ingredientName + "%");
        }

        if (category != null) {
            sql.append(" AND i.category = ?::ingredient_category");
            params.add(category.name());
        }

        if (dishName != null && !dishName.isBlank()) {
            sql.append(" AND d.name ILIKE ?");
            params.add("%" + dishName + "%");
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        try (Connection con = dbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des ingrédients", e);
        }

        return ingredients;
    }


    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }

        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            String checkSql = "SELECT id FROM ingredient WHERE id = ?";
            PreparedStatement psCheck = connection.prepareStatement(checkSql);

            for (Ingredient ing : newIngredients) {

                if (ing.getId() != null) {

                    psCheck.setInt(1, ing.getId());
                    ResultSet rsCheck = psCheck.executeQuery();
                    if (rsCheck.next()) {
                        connection.rollback();
                        throw new RuntimeException("Ingrédient avec ID " + ing.getId() + " existe déjà !");
                    }


                    String insertSql = """
                        INSERT INTO ingredient(id, name, price, category)
                        VALUES (?, ?, ?, ?::ingredient_category)
                        """;
                    try (PreparedStatement psInsert = connection.prepareStatement(insertSql)) {
                        psInsert.setInt(1, ing.getId());
                        psInsert.setString(2, ing.getName());
                        psInsert.setDouble(3, ing.getPrice());
                        psInsert.setString(4, ing.getCategory().name());
                        psInsert.executeUpdate();
                    }

                    updateSequenceNextValue(connection, "ingredient", "id", getSerialSequenceName(connection, "ingredient", "id"));

                } else {

                    String insertSql = """
                        INSERT INTO ingredient(name, price, category)
                        VALUES (?, ?, ?::ingredient_category)
                        RETURNING id
                        """;
                    try (PreparedStatement psInsert = connection.prepareStatement(insertSql)) {
                        psInsert.setString(1, ing.getName());
                        psInsert.setDouble(2, ing.getPrice());
                        psInsert.setString(3, ing.getCategory().name());

                        ResultSet rsInsert = psInsert.executeQuery();
                        if (rsInsert.next()) {
                            ing.setId(rsInsert.getInt(1));
                        }
                    }
                }

                savedIngredients.add(ing);
            }

            connection.commit();
            return savedIngredients;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création des ingrédients", e);
        }
    }


    public Dish saveDish(Dish toSave) {
        String upsertDishSql = """
        INSERT INTO dish (id, name, dish_type, price)
        VALUES (?, ?, ?::dish_type, ?)
        ON CONFLICT (id) DO UPDATE
        SET name = EXCLUDED.name,
            dish_type = EXCLUDED.dish_type,
            price = EXCLUDED.price
        RETURNING id
    """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);

            Integer dishId;
            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }
                ps.setString(2, toSave.getName());
                ps.setString(3, toSave.getDishType().name());
                ps.setDouble(4, toSave.getPrice());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM DishIngredient WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }


            if (toSave.getIngredients() != null && !toSave.getIngredients().isEmpty()) {
                String insertDishIngredientSql = """
                INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit)
                VALUES (?, ?, ?, ?::unit_type)
            """;
                try (PreparedStatement ps = conn.prepareStatement(insertDishIngredientSql)) {
                    for (DishIngredient di : toSave.getIngredients()) {

                        if (di.getIngredient() == null || di.getIngredient().getId() == null) {
                            throw new RuntimeException("Chaque ingrédient doit exister et avoir un ID !");
                        }

                        ps.setInt(1, dishId);
                        ps.setInt(2, di.getIngredient().getId());
                        ps.setDouble(3, di.getQuantity());
                        ps.setString(4, di.getUnit().name());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();

            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient saveIngredient(Ingredient toSave) {
        try (Connection connection = new DBConnection().getConnection()) {

            if (toSave.getId() == null) {
                int ingredientId = getNextSerialValue(connection, "ingredient", "id");
                toSave.setId(ingredientId);
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO ingredient (id, name, price, category)
                    VALUES (?, ?, ?, ?::ingredient_category)
                    ON CONFLICT (id) DO UPDATE
                        SET name = EXCLUDED.name,
                            price = EXCLUDED.price,
                            category = EXCLUDED.category
                    RETURNING id
                    """
            )) {
                ps.setInt(1, toSave.getId());
                ps.setString(2, toSave.getName());
                ps.setDouble(3, toSave.getPrice());
                ps.setString(4, toSave.getCategory().name());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        toSave.setId(rs.getInt(1));
                    }
                }
            }

            if (toSave.getStockMovementList() != null) {
                for (StockMovement movement : toSave.getStockMovementList()) {

                    if (movement.getId() == null) {
                        int movementId = getNextSerialValue(connection, "stockmovement", "id");
                        movement.setId(movementId);
                    }

                    try (PreparedStatement psMove = connection.prepareStatement(
                            """
                            INSERT INTO stockmovement
                            (id, id_ingredient, quantity, type, unit, creation_datetime)
                            VALUES (?, ?, ?, ?::mouvement_type, ?::unit_type, ?)
                            ON CONFLICT (id) DO NOTHING
                            """
                    )) {
                        psMove.setInt(1, movement.getId());
                        psMove.setInt(2, toSave.getId());
                        psMove.setDouble(3, movement.getValue().getQuantity());
                        psMove.setString(4, movement.getType().name());
                        psMove.setString(5, movement.getValue().getUnit().name());

                        Instant instant = movement.getCreationDateTime() != null
                                ? movement.getCreationDateTime()
                                : Instant.now();
                        psMove.setTimestamp(6, Timestamp.from(instant));

                        psMove.executeUpdate();
                    }
                }
            }

            return toSave;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'ingredient", e);
        }
    }

    public Ingredient findIngredientById(Integer ingredientId) {
        DBConnection dbConnection = new DBConnection();

        Ingredient ingredient = null;

        try (Connection connection = dbConnection.getConnection()) {


            String ingredientSql = """
            SELECT id, name, price, category
            FROM ingredient
            WHERE id = ?
        """;

            try (PreparedStatement ps = connection.prepareStatement(ingredientSql)) {
                ps.setInt(1, ingredientId);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ingredient = new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            CategoryEnum.valueOf(rs.getString("category"))
                    );
                }
            }

            if (ingredient == null) {
               throw new RuntimeException("L'ingredient n'existe pas encore dans la base");
            }

            String movementSql = """
            SELECT id, quantity, unit, type, creation_datetime
            FROM stockmovement
            WHERE id_ingredient = ?
        """;

            List<StockMovement> movements = new ArrayList<>();

            try (PreparedStatement ps = connection.prepareStatement(movementSql)) {
                ps.setInt(1, ingredientId);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {

                    StockValue value = new StockValue(
                            rs.getDouble("quantity"),
                            Unit.valueOf(rs.getString("unit"))
                    );

                    StockMovement movement = new StockMovement(
                            rs.getInt("id"),
                            value,
                            MovementTypeEnum.valueOf(rs.getString("type")),
                            rs.getTimestamp("creation_datetime").toInstant()
                    );

                    movements.add(movement);
                }
            }

            ingredient.setStockMovementList(movements);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ingredient;
    }



//   public Order findOrderByReference(String reference) {
//        DBConnection dbConnection = new DBConnection();
//        try (Connection connection = dbConnection.getConnection()) {
//            PreparedStatement preparedStatement = connection.prepareStatement("""
//                    select id, reference, creation_datetime from \"Order\" where reference like ?""");
//            preparedStatement.setString(1, reference);
//            ResultSet resultSet = preparedStatement.executeQuery();
//            if (resultSet.next()) {
//                Order order = new Order();
//                Integer idOrder = resultSet.getInt("id");
//                order.setId(idOrder);
//                order.setReference(resultSet.getString("reference"));
//                order.setCreationDateTime(resultSet.getTimestamp("creation_datetime").toInstant());
//                order.setDishOrders(findDishOrderByIdOrder(idOrder));
//                return order;
//            }
//            throw new RuntimeException("Order not found with reference " + reference);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private List<DishOrder> findDishOrderByIdOrder(Integer idOrder) {
//        DBConnection dbConnection = new DBConnection();
//        Connection connection = dbConnection.getConnection();
//        List<DishOrder> dishOrders = new ArrayList<>();
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement(
//                    """
//                            select id, id_dish, quantity from dishorder where dishorder.id_order = ?
//                            """);
//            preparedStatement.setInt(1, idOrder);
//            ResultSet resultSet = preparedStatement.executeQuery();
//            while (resultSet.next()) {
//                Dish dish = findDishById(resultSet.getInt("id_dish"));
//                DishOrder dishOrder = new DishOrder();
//                dishOrder.setId(resultSet.getInt("id"));
//                dishOrder.setQuantity(resultSet.getInt("quantity"));
//                dishOrder.setDish(dish);
//                dishOrders.add(dishOrder);
//            }
//            dbConnection.closeConnection(connection);
//            return dishOrders;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    public Order saveOrder(Order orderToSave) {
//        String insertOrderSql = """
//        INSERT INTO "Order" (id, reference, creation_datetime)
//        VALUES (?, ?, ?)
//        ON CONFLICT (id) DO UPDATE
//        SET reference = EXCLUDED.reference,
//            creation_datetime = EXCLUDED.creation_datetime
//        RETURNING id
//    """;
//
//        try (Connection conn = new DBConnection().getConnection()) {
//            conn.setAutoCommit(false);
//
//            Integer orderId;
//
//            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql)) {
//                if (orderToSave.getId() != null) {
//                    ps.setInt(1, orderToSave.getId());
//                } else {
//                    ps.setInt(1, getNextSerialValue(conn, "\"Order\"", "id")
//                    );
//                }
//                ps.setString(2, orderToSave.getReference());
//                ps.setTimestamp(3, Timestamp.from(orderToSave.getCreationDateTime()));
//
//                try (ResultSet rs = ps.executeQuery()) {
//                    rs.next();
//                    orderId = rs.getInt(1);
//                }
//            }
//
//            try (PreparedStatement ps = conn.prepareStatement(
//                    "DELETE FROM DishOrder WHERE id_order = ?")) {
//                ps.setInt(1, orderId);
//                ps.executeUpdate();
//            }
//
//            if (orderToSave.getDishOrders() != null && !orderToSave.getDishOrders().isEmpty()) {
//                String insertDishOrderSql = """
//                INSERT INTO DishOrder (id_order, id_dish, quantity)
//                VALUES (?, ?, ?)
//            """;
//                try (PreparedStatement ps = conn.prepareStatement(insertDishOrderSql)) {
//                    for (DishOrder dishOrder : orderToSave.getDishOrders()) {
//                        if (dishOrder.getDish() == null || dishOrder.getDish().getId() == null) {
//                            throw new RuntimeException("Chaque plat doit exister et avoir un ID !");
//                        }
//                        ps.setInt(1, orderId);
//                        ps.setInt(2, dishOrder.getDish().getId());
//                        ps.setInt(3, dishOrder.getQuantity());
//                        ps.addBatch();
//                    }
//                    ps.executeBatch();
//                }
//            }
//
//            conn.commit();
//            return findOrderById(orderId);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }


    public Order findOrderByReference(String reference) {
        DBConnection dbConnection = new DBConnection();
        String sql = """
            SELECT id, reference, creation_datetime, type, status
            FROM "Order"
            WHERE reference = ?
            """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, reference);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Order order = new Order();
                    Integer idOrder = resultSet.getInt("id");
                    order.setId(idOrder);
                    order.setReference(resultSet.getString("reference"));
                    order.setCreationDateTime(resultSet.getTimestamp("creation_datetime").toInstant());


                    String typeStr = resultSet.getString("type");
                    if (typeStr != null) {
                        order.setType(OrderTypeEnum.valueOf(typeStr));
                    }

                    String statusStr = resultSet.getString("status");
                    if (statusStr != null) {
                        order.setStatus(OrderStatusEnum.valueOf(statusStr));
                    }


                    order.setDishOrders(findDishOrderByIdOrder(idOrder));
                    return order;
                }
            }

            throw new RuntimeException("Order not found with reference " + reference);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishOrder> findDishOrderByIdOrder(Integer idOrder) {
        DBConnection dbConnection = new DBConnection();
        List<DishOrder> dishOrders = new ArrayList<>();
        String sql = """
            SELECT id, id_dish, quantity
            FROM DishOrder
            WHERE id_order = ?
            """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idOrder);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Dish dish = findDishById(resultSet.getInt("id_dish"));

                    DishOrder dishOrder = new DishOrder();
                    dishOrder.setId(resultSet.getInt("id"));
                    dishOrder.setQuantity(resultSet.getInt("quantity"));
                    dishOrder.setDish(dish);

                    dishOrders.add(dishOrder);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dishOrders;
    }

    public Order saveOrder(Order orderToSave) {
        String insertOrderSql = """
        INSERT INTO "Order" (id, reference, creation_datetime, type, status)
         VALUES (?, ?, ?, ?::order_type_enum, ?::order_status_enum)
        ON CONFLICT (id) DO UPDATE
        SET reference = EXCLUDED.reference,
            creation_datetime = EXCLUDED.creation_datetime,
            type = EXCLUDED.type,
            status = EXCLUDED.status
        RETURNING id
    """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);

            Integer orderId;

            if (orderToSave.getId() != null) {
                String checkStatusSql = "SELECT status FROM \"Order\" WHERE id = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(checkStatusSql)) {
                    psCheck.setInt(1, orderToSave.getId());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            String currentStatus = rs.getString("status");
                            if (currentStatus != null && currentStatus.equals("DELIVERED")) {
                                throw new RuntimeException("Une commande livrée ne peut plus être modifiée !");
                            }
                        }
                    }
                }
            }


            if (orderToSave.getType() == null) {
                orderToSave.setType(OrderTypeEnum.EAT_IN);
            }
            if (orderToSave.getStatus() == null) {
                orderToSave.setStatus(OrderStatusEnum.CREATED);
            }


            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql)) {
                if (orderToSave.getId() != null) {
                    ps.setInt(1, orderToSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "\"Order\"", "id"));
                }
                ps.setString(2, orderToSave.getReference());
                ps.setTimestamp(3, Timestamp.from(orderToSave.getCreationDateTime()));
                ps.setString(4, orderToSave.getType().name());
                ps.setString(5, orderToSave.getStatus().name());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    orderId = rs.getInt(1);
                }
            }


            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM DishOrder WHERE id_order = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }


            if (orderToSave.getDishOrders() != null && !orderToSave.getDishOrders().isEmpty()) {
                String insertDishOrderSql = """
                INSERT INTO DishOrder (id_order, id_dish, quantity)
                VALUES (?, ?, ?)
            """;
                try (PreparedStatement ps = conn.prepareStatement(insertDishOrderSql)) {
                    for (DishOrder dishOrder : orderToSave.getDishOrders()) {
                        if (dishOrder.getDish() == null || dishOrder.getDish().getId() == null) {
                            throw new RuntimeException("Chaque plat doit exister et avoir un ID !");
                        }
                        ps.setInt(1, orderId);
                        ps.setInt(2, dishOrder.getDish().getId());
                        ps.setInt(3, dishOrder.getQuantity());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return findOrderById(orderId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order findOrderById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Order order = null;

        String orderQuery = """
        SELECT id, reference, creation_datetime, type, status
        FROM "Order"
        WHERE id = ?
    """;

        String dishOrderQuery = """
        SELECT dorder.id as dish_order_id, dorder.id_dish, dorder.quantity,
               d.name as dish_name, d.dish_type, d.price
        FROM DishOrder dorder
        JOIN dish d ON dorder.id_dish = d.id
        WHERE dorder.id_order = ?
    """;

        try (Connection conn = dbConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(orderQuery)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        order = new Order();
                        order.setId(rs.getInt("id"));
                        order.setReference(rs.getString("reference"));
                        order.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());


                        String typeStr = rs.getString("type");
                        if (typeStr != null) {
                            order.setType(OrderTypeEnum.valueOf(typeStr));
                        }

                        String statusStr = rs.getString("status");
                        if (statusStr != null) {
                            order.setStatus(OrderStatusEnum.valueOf(statusStr));
                        }

                        order.setDishOrders(new ArrayList<>());
                    } else {
                        return null;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(dishOrderQuery)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DishOrder dishOrder = new DishOrder();
                        dishOrder.setId(rs.getInt("dish_order_id"));
                        dishOrder.setQuantity(rs.getInt("quantity"));

                        Dish dish = new Dish();
                        dish.setId(rs.getInt("id_dish"));
                        dish.setName(rs.getString("dish_name"));
                        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                        dish.setPrice(rs.getDouble("price"));
                        dishOrder.setDish(findDishById(rs.getInt("id_dish")));
                        order.getDishOrders().add(dishOrder);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la commande par ID", e);
        }

        return order;
    }


//    public Order findOrderById(Integer id) {
//        DBConnection dbConnection = new DBConnection();
//        Order order = null;
//
//
//        String orderQuery = "SELECT id, reference, creation_datetime FROM \"Order\" WHERE id = ?";
//
//
//        String dishOrderQuery = """
//        SELECT dorder.id as dish_order_id, dorder.id_dish, dorder.quantity,
//               d.name as dish_name, d.dish_type, d.price
//        FROM DishOrder dorder
//        JOIN dish d ON dorder.id_dish = d.id
//        WHERE dorder.id_order = ?
//    """;
//
//        try (Connection conn = dbConnection.getConnection()) {
//            try (PreparedStatement ps = conn.prepareStatement(orderQuery)) {
//                ps.setInt(1, id);
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) {
//                        order = new Order();
//                        order.setId(rs.getInt("id"));
//                        order.setReference(rs.getString("reference"));
//                        order.setCreationDateTime(rs.getTimestamp("creation_datetime").toInstant());
//                        order.setDishOrders(new ArrayList<>());
//                    } else {
//                        return null;
//                    }
//                }
//            }
//
//
//            try (PreparedStatement ps = conn.prepareStatement(dishOrderQuery)) {
//                ps.setInt(1, id);
//                try (ResultSet rs = ps.executeQuery()) {
//                    while (rs.next()) {
//                        DishOrder dishOrder = new DishOrder();
//                        dishOrder.setId(rs.getInt("dish_order_id"));
//                        dishOrder.setQuantity(rs.getInt("quantity"));
//
//                        Dish dish = new Dish();
//                        dish.setId(rs.getInt("id_dish"));
//                        dish.setName(rs.getString("dish_name"));
//                        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
//                        dish.setPrice(rs.getDouble("price"));
//
//                        dishOrder.setDish(dish);
//                        order.getDishOrders().add(dishOrder);
//                    }
//                }
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException("Erreur lors de la récupération de la commande par ID", e);
//        }
//
//        return order;
//    }



    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }


    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";

        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 1) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }

    private void attachIngredients(Connection conn, Dish dish, List<DishIngredient> ingredients)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void detachIngredients(Connection conn, Dish dish) throws SQLException {
       throw new UnsupportedOperationException("Not supported yet.");
    }


    // push-down processing

    public StockValue getStockValueAt(Instant t, Integer ingredientIdentifier) {

        DBConnection dbConnection = new DBConnection();
        StockValue stockValue = new StockValue();
        String getStockValueSql = """
              select unit , sum(
              case
                  when stockmovement.type = 'OUT' then stockmovement.quantity * -1
                            else stockmovement.quantity
                            end
                            ) as actual_quantity from stockmovement
              where creation_datetime <= ?
              and stockmovement.id_ingredient = ?
              group by unit
            """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getStockValueSql)) {
            preparedStatement.setTimestamp(1, Timestamp.from(t));
            preparedStatement.setInt(2, ingredientIdentifier);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                stockValue.setUnit(Unit.valueOf(resultSet.getString("unit")));
                stockValue.setQuantity(resultSet.getDouble("actual_quantity"));
            }
            return stockValue;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    public Double getDishCost(Integer dishId){

       DBConnection dbConnection = new DBConnection();
        Double dishIngredientCost = 0.0;
       String getDishCostSql = """
              select dish.name, sum(ingredient.price * dishingredient.quantity_required) as DishIngredientCost from dish
                                                 join dishingredient on dish.id = dishingredient.id_dish
                                                 join ingredient on dishingredient.id_ingredient = ingredient.id
                                                 where id_dish = ?
                                                 group by dish.name;
       """;

       try(Connection connection = dbConnection.getConnection();
       PreparedStatement preparedStatement = connection.prepareStatement(getDishCostSql)){
           preparedStatement.setInt(1, dishId);
           ResultSet resultSet = preparedStatement.executeQuery();
           if (resultSet.next()) {
            dishIngredientCost = resultSet.getDouble("DishIngredientCost");
           }
           return dishIngredientCost ;
       }catch(SQLException e){
           throw new RuntimeException(e);
       }
    }


    public Double getGrossMargint(Integer dishId){
        DBConnection dbConnection = new DBConnection();
        Double dishCrossMargin = 0.0;
        String getDishCrossMarginSql = """
             SELECT dish.price - SUM(ingredient.price * dishingredient.quantity_required) AS Margin
             FROM dish
                      JOIN dishingredient ON dish.id = dishingredient.id_dish
                      JOIN ingredient ON dishingredient.id_ingredient = ingredient.id
             WHERE dish.id = ?
             GROUP BY dish.price;
       """;
        try(Connection connection = dbConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(getDishCrossMarginSql)){
            preparedStatement.setInt(1, dishId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                dishCrossMargin = resultSet.getDouble("Margin");
            }
            return dishCrossMargin ;
        }catch(SQLException e){
        throw new RuntimeException(e);
        }
    }

//    public List<StockMovement> getIngredientStockStats(String period, Instant intervalMin, Instant intervalMax){
//      DBConnection dbConnection = new DBConnection();
//      List<StockMovement> stockMovements = new ArrayList<>();
//
//      String stockStackSql = """
//         SELECT
//                     ingredient.name as name,
//                     DATE_TRUNC(?, stockmovement.creation_datetime) AS period,
//                     SUM(CASE
//                             WHEN stockmovement.type = 'OUT' THEN stockmovement.quantity * -1
//                             ELSE stockmovement.quantity
//                         END) AS stock_quantity, stockmovement.unit
//                 FROM stockmovement
//                          JOIN ingredient
//                         ON stockmovement.id_ingredient = ingredient.id
//                 where stockmovement.creation_datetime BETWEEN ? AND ?
//                 GROUP BY ingredient.name, period ,stockmovement.unit
//                 ORDER BY period;
//      """;
//
//      try (Connection connection = dbConnection.getConnection();
//      PreparedStatement preparedStatement = connection.prepareStatement(stockStackSql)){
//          preparedStatement.setString(1,period.toLowerCase());
//          preparedStatement.setTimestamp(2,Timestamp.from(intervalMin));
//          preparedStatement.setTimestamp(3,Timestamp.from(intervalMax));
//          ResultSet resultSet = preparedStatement.executeQuery();
//          while (resultSet.next()) {
//             stockMovements.add(new StockMovement(
//                     resultSet.getString("name"),
//             ))
//          }
//
//          return stockMovements;
//      }catch (SQLException e) {
//          throw new RuntimeException(e);
//      }
//    }

        public void displayStockResults(String period, Instant startDate, Instant endDate) {

            DBConnection dbConnection = new DBConnection();

            String sql = """
            SELECT
                ingredient.name as name,
                DATE_TRUNC(?, stockmovement.creation_datetime) AS period,
                SUM(CASE
                        WHEN stockmovement.type = 'OUT' THEN stockmovement.quantity * -1
                        ELSE stockmovement.quantity
                    END) AS stock_quantity,
                stockmovement.unit
            FROM stockmovement
            JOIN ingredient ON stockmovement.id_ingredient = ingredient.id
            WHERE stockmovement.creation_datetime BETWEEN ? AND ?
            GROUP BY ingredient.name, period, stockmovement.unit
            ORDER BY period;
        """;

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, period.toLowerCase());
                ps.setTimestamp(2, Timestamp.from(startDate));
                ps.setTimestamp(3, Timestamp.from(endDate));

                ResultSet rs = ps.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")
                        .withZone(ZoneId.systemDefault());

                System.out.printf("%-15s %-15s %-10s %-5s%n", "Ingredient", "Period", "Quantity", "Unit");

                while (rs.next()) {
                    String name = rs.getString("name");
                    Timestamp periodTs = rs.getTimestamp("period");
                    Double quantity = rs.getDouble("stock_quantity");
                    String unit = rs.getString("unit");

                    String periodStr = formatter.format(periodTs.toInstant());

                    System.out.printf("%-15s %-15s %-10.2f %-5s%n", name, periodStr, quantity, unit);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

