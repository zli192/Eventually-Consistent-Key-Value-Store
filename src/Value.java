import java.sql.Timestamp;

public class Value {
	
	String value;
	Timestamp timestamp;
	
	public Value(String value, Timestamp timestamp) {
		this.value = value;
		this.timestamp = timestamp;
	}

	public String getValue() {
		return value;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

}
