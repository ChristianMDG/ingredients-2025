import java.util.List;

public class Main {
    public static void main(String[] args) {

        DataRetriever dataRetriever = new DataRetriever();
        Dish dish = dataRetriever.findDishById(4);
        System.out.println(dish);
        System.out.println(dish.getDishCost());
        System.out.println(dish.getGrossMargin());
    }
}
