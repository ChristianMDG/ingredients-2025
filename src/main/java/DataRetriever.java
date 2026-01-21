import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {

    DBConnection dbConnection =  new DBConnection();

    public Dish findDishById(Integer id) {

        String sql = """
        SELECT
            d.id,
            d.name,
            d.price,
            d.dish_type,

            di.quantity_required,
            di.unit,

            i.id AS ingredient_id,
            i.name AS ingredient_name,
            i.price AS ingredient_price,
            i.category
        FROM dish d
        JOIN dishingredient di ON di.id_dish = d.id
        JOIN ingredient i ON di.id_ingredient = i.id
        WHERE d.id = ?
        """;
        Dish dish = null;
        List<DishIngredient> ingredients = new ArrayList<>();

        try (Connection con = dbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                // Création du Dish une seule fois
                if (dish == null) {
                    dish = new Dish(
                            rs.getInt("id"),
                            rs.getString("name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            rs.getDouble("price"),
                            ingredients
                    );
                }
                Ingredient ingredient = new Ingredient(
                        rs.getInt("ingredient_id"),
                        rs.getString("ingredient_name"),
                        rs.getDouble("ingredient_price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                );


                ingredients.add(new DishIngredient(
                        rs.getInt("id"),
                        dish,
                        ingredient,
                        rs.getDouble("quantity_required"),
                        Unit.valueOf(rs.getString("unit"))
                ));
            }

        } catch (SQLException e) {
           throw new RuntimeException("SQL Error: " + e.getMessage());
        }

        return dish;
    }

    public List<Ingredient> findIngredients(int page, int size) {

        List<Ingredient> ingredients = new ArrayList<>();

        String sql = """
        SELECT id, name, price, category
        FROM ingredient
        LIMIT ? OFFSET ?
    """;

        int offset = (page - 1) * size;

        try (Connection con = dbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ingredients;
    }


//        public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
//        if (newIngredients == null || newIngredients.isEmpty()) {
//            return List.of();
//        }
//        List<Ingredient> savedIngredients = new ArrayList<>();
//        DBConnection dbConnection = new DBConnection();
//        Connection conn = dbConnection.getConnection();
//        try {
//            conn.setAutoCommit(false);
//            String insertSql = """
//                        INSERT INTO ingredient (id, name,price, category)
//                        VALUES (?, ?, ?::ingredient_category, ?)
//                        RETURNING id
//                    """;
//            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
//                for (Ingredient ingredient : newIngredients) {
//                    ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
//                    ps.setString(2, ingredient.getName());
//                    ps.setDouble(3, ingredient.getPrice());
//                    ps.setString(4, ingredient.getCategory().name());
//
//
//                    try (ResultSet rs = ps.executeQuery()) {
//                        rs.next();
//                        int generatedId = rs.getInt(1);
//                        ingredient.setId(generatedId);
//                        savedIngredients.add(ingredient);
//                    }
//                }
//                conn.commit();
//                return savedIngredients;
//            } catch (SQLException e) {
//                conn.rollback();
//                throw new RuntimeException(e);
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } finally {
//            dbConnection.closeConnection(conn);
//        }
//    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();


        String sql = """
        SELECT DISTINCT d.id, d.name, d.price, d.dish_type
        FROM dish d
        JOIN dishingredient di ON di.id_dish = d.id
        JOIN ingredient i ON i.id = di.id_ingredient
        WHERE i.name ILIKE ?
    """;

        try (Connection con = dbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {


            ps.setString(1, "%" + ingredientName + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish(
                        rs.getInt("id"),
                        rs.getString("name"),
                        DishTypeEnum.valueOf(rs.getString("dish_type")),
                        rs.getDouble("price"),
                        new ArrayList<>()
                );
                dishes.add(dish);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des plats : " + e.getMessage());
        }

        return dishes;
    }


    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size) {

        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("""
        SELECT DISTINCT i.id, i.name, i.price, i.category
        FROM ingredient i
        LEFT JOIN dishingredient di ON di.id_ingredient = i.id
        LEFT JOIN dish d ON di.id_dish = d.id
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();


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
                Object param = params.get(i);
                if (param instanceof String s) {
                    ps.setString(i + 1, s);
                } else if (param instanceof Integer n) {
                    ps.setInt(i + 1, n);
                } else {
                    throw new RuntimeException("Type de paramètre non géré : " + param);
                }
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
            throw new RuntimeException("Erreur lors de la recherche des ingrédients : " + e.getMessage());
        }

        return ingredients;
    }



//    public List<DishIngredient> findDishIngredientsDishId(Integer dishId) {
//
//        List<DishIngredient> dishIngredients = new ArrayList<>();
//
//        String findDishIngredientsSql = """
//        SELECT
//            di.id_ingredient,
//            i.name,
//            di.quantity_required,
//            di.unit
//        FROM dishingredient di
//        JOIN ingredient i ON di.id_ingredient = i.id
//        WHERE di.id_dish = ?
//    """;
//
//        try (PreparedStatement ps = dbConnection.getConnection().prepareStatement(findDishIngredientsSql)) {
//
//            // 1. Passer le paramètre ?
//            ps.setInt(1, dishId);
//
//            // 2. Exécuter la requête
//            ResultSet rs = ps.executeQuery();
//
//            // 3. Parcourir le résultat
//            while (rs.next()) {
//
//                // Ingredient
//                Ingredient ingredient = new Ingredient();
//                ingredient.setId(rs.getInt("id_ingredient"));
//                ingredient.setName(rs.getString("name"));
//
//                // DishIngredient
//                DishIngredient dishIngredient = new DishIngredient();
//                dishIngredient.setIngredient(ingredient);
//                dishIngredient.setQuantity(rs.getDouble("quantity_required"));
//                Unit.valueOf(rs.getString("unit"));
//
//                dishIngredients.add(dishIngredient);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return dishIngredients;
//    }


    private Ingredient mapIngredient(ResultSet rs) throws SQLException {

        return new Ingredient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                CategoryEnum.valueOf(rs.getString("category"))
        );
    }

    private DishIngredient mapDishIngredient(
            ResultSet rs, Dish dish) throws SQLException {

        Ingredient ingredient = mapIngredient(rs);

        return new DishIngredient(
                dish,
                ingredient,
                rs.getDouble("quantity_required"),
                Unit.valueOf(rs.getString("unit"))
        );
    }




    public Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                    INSERT INTO dish (id, price, name, dish_type)
                    VALUES (?, ?, ?, ?::dish_type)
                    ON CONFLICT (id) DO UPDATE
                    SET name = EXCLUDED.name,
                        dish_type = EXCLUDED.dish_type
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
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            List<Ingredient> newIngredients = toSave.getIngredients();
            detachIngredients(conn, dishId, newIngredients);
            attachIngredients(conn, dishId, newIngredients);

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }
        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            String insertSql = """
                        INSERT INTO ingredient (id, name, category, price, required_quantity)
                        VALUES (?, ?, ?::ingredient_category, ?, ?)
                        RETURNING id
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());


                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }


    private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            return;
        }

        String baseSql = """
                    UPDATE ingredient
                    SET id_dish = NULL
                    WHERE id_dish = ? AND id NOT IN (%s)
                """;

        String inClause = ingredients.stream()
                .map(i -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(baseSql, inClause);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            int index = 2;
            for (Ingredient ingredient : ingredients) {
                ps.setInt(index++, ingredient.getId());
            }
            ps.executeUpdate();
        }
    }

    private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        String attachSql = """
                    UPDATE ingredient
                    SET id_dish = ?
                    WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (Ingredient ingredient : ingredients) {
                ps.setInt(1, dishId);
                ps.setInt(2, ingredient.getId());
                ps.addBatch(); // Can be substitute ps.executeUpdate() but bad performance
            }
            ps.executeBatch();
        }
    }

    private List<Ingredient> findIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select ingredient.id, ingredient.name, ingredient.price, ingredient.category, ingredient.required_quantity
                            from ingredient where id_dish = ?;
                            """);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


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
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }

}
