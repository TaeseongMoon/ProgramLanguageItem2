package interpreter;

public class UndefinedException extends Exception{
    public UndefinedException(String id){
        super("NameError: name '"+id+"' is not defined");
    }
}
