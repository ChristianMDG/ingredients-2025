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

    public List<Ingredient> findIngredientsByCriteria(String ingredientName,
                              CategoryEnum category, String dishName, int page, int size){
       throw new RuntimeException("Not implemented");
    }
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        throw new RuntimeException("Not implemented");
    }

    public Dish saveDish(Dish toSave) {
        throw new RuntimeException("Not implemented");
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



}
