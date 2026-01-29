import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement>  stockMovementList;

    public Ingredient() {}
    public Ingredient(String name, Double price, CategoryEnum category, List<StockMovement> stockMovementList) {
        this.name = name;
        this.price = price;
        this.category = category;
    }
    public Ingredient(Integer id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;

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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

//    public StockValue getStockValueAt(Instant t) {
//        if(stockMovementList == null) return null;
//
//        Set<Unit> units = new HashSet<>();
//
//        for (StockMovement stockMovement : stockMovementList) {
//            Unit unit = stockMovement.getValue().getUnit();
//            units.add(unit);
//        }
//
//        if (units.size() > 1) {
//            throw new RuntimeException("Erreur unit");
//        }
//
//        double stock = 0.0;
//
//        if (stockMovementList != null) {
//            for (StockMovement movement : stockMovementList) {
//
//                if (movement.getCreationDateTime() != null
//                        && movement.getValue() != null
//                        && movement.getValue().getQuantity() != null
//                        && !movement.getCreationDateTime().isAfter(t)) {
//
//                    double quantity = movement.getValue().getQuantity();
//
//                    if (movement.getType() == MovementTypeEnum.OUT) {
//                        quantity = -quantity;
//                    }
//
//                    stock += quantity;
//                }
//            }
//        }
//        return new StockValue(stock,);
//    }
//
//    public StockValue getStockValueAt(Instant t) {
//        if (stockMovementList == null) return null;
//        Map<Unit, List<StockMovement>> unitSet = stockMovementList.stream()
//                .collect(Collectors.groupingBy(stockMovement -> stockMovement.getValue().getUnit()));
//        if (unitSet.keySet().size() > 1) {
//            throw new RuntimeException("Multiple unit found and not handle for conversion");
//        }
//
//        List<StockMovement> stockMovements = stockMovementList.stream()
//                .filter(stockMovement -> !stockMovement.getCreationDateTime().isAfter(t))
//                .toList();
//        double movementIn = stockMovements.stream()
//                .filter(stockMovement -> stockMovement.getType().equals(MovementTypeEnum.IN))
//                .flatMapToDouble(stockMovement -> DoubleStream.of(stockMovement.getValue().getQuantity()))
//                .sum();
//        double movementOut = stockMovements.stream()
//                .filter(stockMovement -> stockMovement.getType().equals(MovementTypeEnum.OUT))
//                .flatMapToDouble(stockMovement -> DoubleStream.of(stockMovement.getValue().getQuantity()))
//                .sum();
//
//        StockValue stockValue = new StockValue();
//        stockValue.setQuantity(movementIn - movementOut);
//        stockValue.setUnit(unitSet.keySet().stream().findFirst().get());
//
//        return stockValue;
//    }



    public StockValue getStockValueAt(Instant t) {
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            return null;
        }
        Set<Unit> units = new HashSet<>();
        double stock = 0.0;

        for (StockMovement movement : stockMovementList) {

            if (movement == null
                    || movement.getCreationDateTime() == null
                    || movement.getValue() == null
                    || movement.getValue().getQuantity() == null
                    || movement.getValue().getUnit() == null) {
                continue;
            }

            units.add(movement.getValue().getUnit());

            if (movement.getCreationDateTime().isAfter(t)) {
                continue;
            }

            double quantity = movement.getValue().getQuantity();

            if (movement.getType() == MovementTypeEnum.OUT) {
                quantity = -quantity;
            }

            stock += quantity;
        }

        if (units.size() > 1) {
            throw new RuntimeException("Multiple unit found and not handle for conversion");
        }
        return new StockValue(stock, units.iterator().next());
    }

    public Double getAvailableStock(Ingredient ingredient) {
        double stock = 0.0;
        for (StockMovement movement : ingredient.getStockMovementList()) {
            if (movement.getType() == MovementTypeEnum.IN) {
                stock += movement.getValue().getQuantity();
            } else if (movement.getType() == MovementTypeEnum.OUT) {
                stock -= movement.getValue().getQuantity();
            }
        }
        return stock;
    }


    public void prettyPrint() {
        System.out.println("   - ID        : " + id);
        System.out.println("   - Catégorie : " + category);
        System.out.println("   - Prix      : " + price + " Ar");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(price, that.price) && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, category);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                '}';
    }
}
