import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        throw new UnsupportedOperationException("Not supported yet.");
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


    private void attachIngredients(Connection conn, Dish dish, List<DishIngredient> ingredients)
            throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void detachIngredients(Connection conn, Dish dish) throws SQLException {
       throw new UnsupportedOperationException("Not supported yet.");
    }


}
