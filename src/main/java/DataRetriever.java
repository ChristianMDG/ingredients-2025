import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {


    public Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        try (Connection connection = dbConnection.getConnection()) {
            //recuperer le plat
            PreparedStatement psDish = connection.prepareStatement(
                    "SELECT id, name, dish_type, price FROM dish WHERE id = ?"
            );
            psDish.setInt(1, id);
            ResultSet rsDish = psDish.executeQuery();

            if (!rsDish.next()) {
                throw new RuntimeException("Dish not found: " + id);
            }

            Dish dish = new Dish();
            dish.setId(rsDish.getInt("id"));
            dish.setName(rsDish.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rsDish.getString("dish_type")));
            dish.setPrice(rsDish.getObject("price") == null ? null : rsDish.getDouble("price"));

           //recuperer les ingredients du plat
            PreparedStatement psIngredients = connection.prepareStatement(
                    """
                    SELECT i.id, i.name, i.price, i.category, di.quantity_required
                    FROM ingredient i
                    JOIN dishingredient di ON i.id = di.id_ingredient
                    WHERE di.id_dish = ?
                    """
            );
            psIngredients.setInt(1, id);
            ResultSet rsIngredients = psIngredients.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (rsIngredients.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rsIngredients.getInt("id"));
                ingredient.setName(rsIngredients.getString("name"));
                ingredient.setPrice(rsIngredients.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rsIngredients.getString("category")));
                ingredient.setQuantity(rsIngredients.getDouble("quantity_required"));
                ingredients.add(ingredient);
            }

            dish.setIngredients(ingredients);

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


//
//    Dish saveDish(Dish toSave) {
//        String upsertDishSql = """
//                    INSERT INTO dish (id, price, name, dish_type)
//                    VALUES (?, ?, ?, ?::dish_type)
//                    ON CONFLICT (id) DO UPDATE
//                    SET name = EXCLUDED.name,
//                        dish_type = EXCLUDED.dish_type
//                    RETURNING id
//                """;
//
//        try (Connection conn = new DBConnection().getConnection()) {
//            conn.setAutoCommit(false);
//            Integer dishId;
//            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
//                if (toSave.getId() != null) {
//                    ps.setInt(1, toSave.getId());
//                } else {
//                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
//                }
//                if (toSave.getPrice() != null) {
//                    ps.setDouble(2, toSave.getPrice());
//                } else {
//                    ps.setNull(2, Types.DOUBLE);
//                }
//                ps.setString(3, toSave.getName());
//                ps.setString(4, toSave.getDishType().name());
//                try (ResultSet rs = ps.executeQuery()) {
//                    rs.next();
//                    dishId = rs.getInt(1);
//                }
//            }
//
//            List<Ingredient> newIngredients = toSave.getIngredients();
//            detachIngredients(conn, dishId, newIngredients);
//            attachIngredients(conn, dishId, newIngredients);
//
//            conn.commit();
//            return findDishById(dishId);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
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
//                    if (ingredient.getQuantity() != null) {
//                        ps.setDouble(5, ingredient.getQuantity());
//                    }else {
//                        ps.setNull(5, Types.DOUBLE);
//                    }
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
//    private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
//            throws SQLException {
//        if (ingredients == null || ingredients.isEmpty()) {
//            try (PreparedStatement ps = conn.prepareStatement(
//                    "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?")) {
//                ps.setInt(1, dishId);
//                ps.executeUpdate();
//            }
//            return;
//        }
//
//        String baseSql = """
//                    UPDATE ingredient
//                    SET id_dish = NULL
//                    WHERE id_dish = ? AND id NOT IN (%s)
//                """;
//
//        String inClause = ingredients.stream()
//                .map(i -> "?")
//                .collect(Collectors.joining(","));
//
//        String sql = String.format(baseSql, inClause);
//
//        try (PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setInt(1, dishId);
//            int index = 2;
//            for (Ingredient ingredient : ingredients) {
//                ps.setInt(index++, ingredient.getId());
//            }
//            ps.executeUpdate();
//        }
//    }
//
//    private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
//            throws SQLException {
//
//        if (ingredients == null || ingredients.isEmpty()) {
//            return;
//        }
//
//        String attachSql = """
//                    UPDATE ingredient
//                    SET id_dish = ?
//                    WHERE id = ?
//                """;
//
//        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
//            for (Ingredient ingredient : ingredients) {
//                ps.setInt(1, dishId);
//                ps.setInt(2, ingredient.getId());
//                ps.addBatch(); // Can be substitute ps.executeUpdate() but bad performance
//            }
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
}
