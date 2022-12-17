/*
  Error.java
  Permite regresar al cliente REST un mensaje de error
  Carlos Pineda Guerrero, noviembre 2022
*/

package ecommerce;

public class Error {
	String message;

	Error(String message) {
		this.message = message;
	}
}
