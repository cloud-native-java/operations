package demo.metrics;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@Entity
public class Customer {

 @Id
 @GeneratedValue
 private Long id;

 private String email, name;

 Customer(String e, String n) {
  this.email = e;
  this.name = n;
 }
}
