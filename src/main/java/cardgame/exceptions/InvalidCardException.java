package main.java.cardgame.exceptions;

public class InvalidCardException extends RuntimeException{


    public InvalidCardException(String message) {
        super(message);
    }

    public InvalidCardException() {
    }

}
