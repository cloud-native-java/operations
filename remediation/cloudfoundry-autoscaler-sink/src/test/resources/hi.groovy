
@RestController
class GreetingsRestController {

      @GetMapping("/hi")
      def hi(){
      	  return "Hello world!";
      }
}