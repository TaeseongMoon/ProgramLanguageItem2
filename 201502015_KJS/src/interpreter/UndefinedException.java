package interpreter;

public class UndefinedException extends Exception{
    public UndefinedException(String id){
        super(id+" is undefined");
    }
}
