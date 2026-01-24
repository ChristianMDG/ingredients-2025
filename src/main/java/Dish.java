import java.util.List;
import java.util.Objects;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double price;
    private List<DishIngredient> ingredients;


    public Dish(){}

    public Dish(int id, String name, DishTypeEnum dishType,Double price, List<DishIngredient> ingredients) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<DishIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<DishIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Double getDishCost() {
        double cost = 0.0;
        if (ingredients == null || ingredients.isEmpty()) {
            return 0.0;
        }

       for (DishIngredient ingredient : ingredients) {
           if (ingredient.getQuantity() != null && ingredient.getIngredient() != null && ingredient.getIngredient().getPrice() != null) {
               cost += ingredient.getIngredient().getPrice() * ingredient.getQuantity();
           }
       }

        return cost;
    }


    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Dish selling price is null");
        }
        return price - getDishCost();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) && Objects.equals(price, dish.price) && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(ingredients, dish.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, name, dishType, ingredients);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", price=" + price +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredients +
                '}';
    }
}
