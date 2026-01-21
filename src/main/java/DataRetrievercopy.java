//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class DataRetrievercopy {
//
//    DBConnection dbConnection =  new DBConnection();
//
//    public Dish findDishById(Integer id) {
//
//        String sql = """
//        SELECT
//            d.id,
//            d.name,
//            d.price,
//            d.dish_type,
//
//            di.quantity_required,
//            di.unit,
//
//            i.id AS ingredient_id,
//            i.name AS ingredient_name,
//            i.price AS ingredient_price,
//            i.category
//        FROM dish d
//        JOIN dishingredient di ON di.id_dish = d.id
//        JOIN ingredient i ON di.id_ingredient = i.id
//        WHERE d.id = ?
//        """;
//        Dish dish = null;
//        List<DishIngredient> ingredients = new ArrayList<>();
//
//        try (Connection con = dbConnection.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, id);
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//
//                // Création du Dish une seule fois
//                if (dish == null) {
//                    dish = new Dish(
//                            rs.getInt("id"),
//                            rs.getString("name"),
//                            DishTypeEnum.valueOf(rs.getString("dish_type")),
//                            rs.getDouble("price"),
//                            ingredients
//                    );
//                }
//
//                Ingredient ingredient = new Ingredient(
//                        rs.getInt("ingredient_id"),
//                        rs.getString("ingredient_name"),
//                        rs.getDouble("ingredient_price"),
//                        CategoryEnum.valueOf(rs.getString("category"))
//                );
//
//
//                ingredients.add(new DishIngredient(
//                        rs.getInt("id"),
//                        dish,
//                        ingredient,
//                        rs.getDouble("quantity_required"),
//                        Unit.valueOf(rs.getString("unit"))
//                ));
//            }
//
//        } catch (SQLException e) {
//           throw new RuntimeException("SQL Error: " + e.getMessage());
//        }
//
//        return dish;
//    }
//
//
//    private List<Ingredient> findIngredientByDishId(Integer idDish) {
//        DBConnection dbConnection = new DBConnection();
//        Connection connection = dbConnection.getConnection();
//        List<Ingredient> ingredients = new ArrayList<>();
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement(
//                    """
//                            select ingredient.id, ingredient.name, ingredient.price, ingredient.category, ingredient.required_quantity
//                            from ingredient where id_dish = ?;
//                            """);
//            preparedStatement.setInt(1, idDish);
//            ResultSet resultSet = preparedStatement.executeQuery();
//            while (resultSet.next()) {
//                Ingredient ingredient = new Ingredient();
//                ingredient.setId(resultSet.getInt("id"));
//                ingredient.setName(resultSet.getString("name"));
//                ingredient.setPrice(resultSet.getDouble("price"));
//                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
//                ingredients.add(ingredient);
//            }
//            dbConnection.closeConnection(connection);
//            return ingredients;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//
//    public Dish saveDish(Dish toSave) {
//
//        String upsertDishSql = """
//        INSERT INTO dish (id, price, name, dish_type)
//        VALUES (?, ?, ?, ?::dish_type)
//        ON CONFLICT (id) DO UPDATE
//        SET name = EXCLUDED.name,
//            price = EXCLUDED.price,
//            dish_type = EXCLUDED.dish_type
//        RETURNING id
//        """;
//
//        try (Connection conn = dbConnection.getConnection()) {
//            conn.setAutoCommit(false);
//
//            Integer dishId;
//
//            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
//
//                ps.setInt(1,
//                        toSave.getId() > 0
//                                ? toSave.getId()
//                                : getNextSerialValue(conn, "dish", "id")
//                );
//
//                if (toSave.getPrice() != null) {
//                    ps.setDouble(2, toSave.getPrice());
//                } else {
//                    ps.setNull(2, Types.DOUBLE);
//                }
//
//                ps.setString(3, toSave.getName());
//                ps.setString(4, toSave.getDishType().name());
//
//                try (ResultSet rs = ps.executeQuery()) {
//                    rs.next();
//                    dishId = rs.getInt(1);
//                }
//            }
//
//            // 🔹 CORRECTION ICI
//            List<DishIngredient> dishIngredients = toSave.getIngredients();
//
//            detachIngredients(conn, dishId);
//            attachIngredients(conn, dishId, dishIngredients);
//
//            conn.commit();
//            return findDishById(dishId);
//
//        } catch (SQLException e) {
//            throw new RuntimeException("Erreur lors du saveDish", e);
//        }
//    }
//
//    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
//        if (newIngredients == null || newIngredients.isEmpty()) {
//            return List.of();
//        }
//        List<Ingredient> savedIngredients = new ArrayList<>();
//        DBConnection dbConnection = new DBConnection();
//        Connection conn = dbConnection.getConnection();
//        try {
//            conn.setAutoCommit(false);
//            String insertSql = """
//                        INSERT INTO ingredient (id, name, category, price, required_quantity)
//                        VALUES (?, ?, ?::ingredient_category, ?, ?)
//                        RETURNING id
//                    """;
//            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
//                for (Ingredient ingredient : newIngredients) {
//                    if (ingredient.getId() != null) {
//                        ps.setInt(1, ingredient.getId());
//                    } else {
//                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
//                    }
//                    ps.setString(2, ingredient.getName());
//                    ps.setString(3, ingredient.getCategory().name());
//                    ps.setDouble(4, ingredient.getPrice());
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
//
//
//private void detachIngredients(Connection conn, Integer dishId)
//        throws SQLException {
//
//    String sql = "DELETE FROM dishingredient WHERE id_dish = ?";
//
//    try (PreparedStatement ps = conn.prepareStatement(sql)) {
//        ps.setInt(1, dishId);
//        ps.executeUpdate();
//    }
//}
//
//    private void attachIngredients(
//            Connection conn,
//            Integer dishId,
//            List<DishIngredient> ingredients
//    ) throws SQLException {
//
//        if (ingredients == null || ingredients.isEmpty()) {
//            return;
//        }
//
//        String sql = """
//        INSERT INTO dishingredient
//        (id_dish, id_ingredient, quantity_required, unit)
//        VALUES (?, ?, ?, ?)
//        """;
//
//        try (PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            for (DishIngredient di : ingredients) {
//                ps.setInt(1, dishId);
//                ps.setInt(2, di.getIngredient().getId());
//                ps.setDouble(3, di.getQuantity());
//                ps.setString(4, di.getUnit().name());
//                ps.addBatch();
//            }
//
//            ps.executeBatch();
//        }
//    }
//
//
//    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
//            throws SQLException {
//
//        String sql = "SELECT pg_get_serial_sequence(?, ?)";
//
//        try (PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setString(1, tableName);
//            ps.setString(2, columnName);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getString(1);
//                }
//            }
//        }
//        return null;
//    }
//
//    private int getNextSerialValue(Connection conn, String tableName, String columnName)
//            throws SQLException {
//
//        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
//        if (sequenceName == null) {
//            throw new IllegalArgumentException(
//                    "Any sequence found for " + tableName + "." + columnName
//            );
//        }
//        updateSequenceNextValue(conn, tableName, columnName, sequenceName);
//
//        String nextValSql = "SELECT nextval(?)";
//
//        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
//            ps.setString(1, sequenceName);
//            try (ResultSet rs = ps.executeQuery()) {
//                rs.next();
//                return rs.getInt(1);
//            }
//        }
//    }
//
//    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
//        String setValSql = String.format(
//                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
//                sequenceName, columnName, tableName
//        );
//
//        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
//            ps.executeQuery();
//        }
//    }
//
//        private List<DishIngredient> findDishIngredientByDishId(Integer dishId) {
//
//        String sql = """
//        SELECT
//            di.quantity_required,
//            di.unit,
//
//            i.id AS ingredient_id,
//            i.name AS ingredient_name,
//            i.price AS ingredient_price,
//            i.category
//        FROM dishingredient di
//        JOIN ingredient i ON di.id_ingredient = i.id
//        WHERE di.id_dish = ?
//        """;
//
//        List<DishIngredient> dishIngredients = new ArrayList<>();
//
//        try (Connection con = dbConnection.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, dishId);
//
//            try (ResultSet rs = ps.executeQuery()) {
//
//                while (rs.next()) {
//
//                    Ingredient ingredient = new Ingredient(
//                            rs.getInt("ingredient_id"),
//                            rs.getString("ingredient_name"),
//                            rs.getDouble("ingredient_price"),
//                            CategoryEnum.valueOf(rs.getString("category"))
//                    );
//
//                    DishIngredient dishIngredient = new DishIngredient();
//                    dishIngredient.setIngredient(ingredient);
//                    dishIngredient.setQuantity(rs.getDouble("quantity_required"));
//                    dishIngredient.setUnit(Unit.valueOf(rs.getString("unit")));
//
//                    dishIngredients.add(dishIngredient);
//                }
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException("Erreur findDishIngredientByDishId", e);
//        }
//
//        return dishIngredients;
//    }
//
//
//
//}
