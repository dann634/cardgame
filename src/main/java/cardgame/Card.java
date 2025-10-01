package main.java.cardgame;

import main.java.cardgame.exceptions.InvalidCardException;

public class Card {
    private final Integer number;

    public Card(Integer number) {
        if (number == null) {
            throw new NullPointerException("Card cannot have null value");
        }

        if(number <= 0) {
            throw new InvalidCardException("Card number cannot be less than 1");
        }

        this.number = number;
    }

    public Integer getNumber() {
        return this.number;
    }
}
