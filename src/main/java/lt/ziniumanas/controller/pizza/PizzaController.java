package lt.ziniumanas.controller.pizza;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pizzas") //localhost:8080/pizzas
public class PizzaController {
//    private final PizzaService pizzaService;
//
//    public PizzaController(PizzaService pizzaService) {
//        this.pizzaService = pizzaService;
//    }
//
//    @GetMapping("/all") //localhost:8080/pizzas/all
//    public ResponseEntity<List<Pizza>> getAll(){
//        return ResponseEntity.ok(pizzaService.getAll());
//    }
//
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Pizza> getById(@PathVariable Long id){
//        return ResponseEntity.of( pizzaService.getById(id) );
//    }
//
//    @PostMapping
//    public ResponseEntity<Pizza> create(@RequestBody  Pizza pizza){
//        Pizza created = pizzaService.create(pizza);
//        return ResponseEntity.ok(created);
//    }
}