public class Ticket {
    private int id;
    private String description;

    // Constructor to initialize the ticket with an id and description
    public Ticket(int id, String description) {
        this.id = id;
        this.description = description;
    }

    //getters and setter for the id and description
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Override the toString method to provide a string representation of the ticket
    @Override
    public String toString() {
        return "Ticket{id=" + id + ", description='" + description + "'}";
    }
}