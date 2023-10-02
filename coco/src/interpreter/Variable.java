package interpreter;

import java.util.HashSet;
import java.util.Set;

import coco.Token;
import coco.Token.Kind;

public class Variable {

	private Object value;
	private Class<?> type;
	
	private Variable(Class<?> type, Object value) {
		this.type = type;
		this.value = type.cast(value);
	};
	
	public Variable() {
		value = null;
		type = void.class;
	}
	
	public Variable(Kind kind) throws ValueException {
		type = getTypeClass(kind);
	}
	
	public Variable(Kind kind, Object value) throws ValueException {
		type = getTypeClass(kind);
		this.value = type.cast(value);
	}
	
	public void setValue(Object newValue) {
		value = type.cast(newValue);
	}
	
	public void setValue(Variable newValue) throws IncompatibleTypeException {
		if(VALID_TYPE_SET.contains(new TypePair(newValue.type, type))) {
			value = type.cast(newValue.value);
		} else {
			throw new IncompatibleTypeException();
		}
	}
	
	public Object getValue() {
		return value;
	}
	
	public boolean isType(Class<?> type) {
		return this.type == type;
	}
	
	public String getTypeName() {
		if(type == Integer.class) {
			return "INTEGER";
		} else if(type == Float.class) {
			return "FLOAT";
		} else if(type == Boolean.class) {
			return "BOOLEAN";
		} else {
			return "UNKNOWN";
		}
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	private static final Set<TypePair> VALID_TYPE_SET = new HashSet<>();
	static {
		VALID_TYPE_SET.add(new TypePair(Integer.class, Integer.class));
		VALID_TYPE_SET.add(new TypePair(Integer.class, Float.class));
		VALID_TYPE_SET.add(new TypePair(Float.class, Float.class));
		VALID_TYPE_SET.add(new TypePair(Boolean.class, Boolean.class));
	}
	
	private static Class<?> getTypeClass(Kind type) throws ValueException {
		if(type == Kind.BOOL) {
			return Boolean.class;
		} else if(type == Kind.INT) {
			return Integer.class;
		} else if(type == Kind.FLOAT) {
			return Float.class;
		} else {
			throw new ValueException();
		}
	}
	
	public Variable not() throws InvalidOperationException {
		if(type != Boolean.class) {
			throw new InvalidOperationException();
		}
		return new Variable(type, !(boolean)value);
	}
	
	public Variable pow(Variable exp) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((exp.type != Integer.class) && (exp.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> newType = type == exp.type ? type : Float.class;
		double newValue = Math.pow(((Number)value).doubleValue(), ((Number)exp.value).doubleValue());
		if(newType == Integer.class) {
			return new Variable(newType, Integer.valueOf((int)newValue));
		} else {
			return new Variable(newType, Float.valueOf((int)newValue));
		}
	}
	
	public Variable multiply(Variable multiplier) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((multiplier.type != Integer.class) && (multiplier.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> newType = type == multiplier.type ? type : Float.class;
		if(newType == Integer.class) {
			return new Variable(newType, (int)this.value * (int)multiplier.value);
		} else {
			return new Variable(newType, (float)this.value * (float)multiplier.value);
		}
	}
	
	public Variable divide(Variable divisor) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((divisor.type != Integer.class) && (divisor.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> newType = type == divisor.type ? type : Float.class;
		if(newType == Integer.class) {
			return new Variable(newType, (int)this.value / (int)divisor.value);
		} else {
			return new Variable(newType, (float)this.value / (float)divisor.value);
		}
	}
	
	public Variable mod(Variable divisor) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((divisor.type != Integer.class) && (divisor.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> newType = type == divisor.type ? type : Float.class;
		if(newType == Integer.class) {
			return new Variable(newType, (int)this.value % (int)divisor.value);
		} else {
			return new Variable(newType, (float)this.value % (float)divisor.value);
		}
	}
	
	public Variable and(Variable other) throws InvalidOperationException {
		if((type != Boolean.class) || (other.type != Boolean.class)) {
			throw new InvalidOperationException();
		}
		return new Variable(Boolean.class, (boolean)this.value && (boolean)other.value);
	}
	
	public Variable add(Variable addend) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((addend.type != Integer.class) && (addend.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> newType = type == addend.type ? type : Float.class;
		if(newType == Integer.class) {
			return new Variable(newType, (int)this.value + (int)addend.value);
		} else {
			return new Variable(newType, (float)this.value + (float)addend.value);
		}
	}
	
	public Variable subtract(Variable subtrahend) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((subtrahend.type != Integer.class) && (subtrahend.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> newType = type == subtrahend.type ? type : Float.class;
		if(newType == Integer.class) {
			return new Variable(newType, (int)this.value - (int)subtrahend.value);
		} else {
			return new Variable(newType, (float)this.value - (float)subtrahend.value);
		}
	}
	
	public Variable or(Variable other) throws InvalidOperationException {
		if((type != Boolean.class) || (other.type != Boolean.class)) {
			throw new InvalidOperationException();
		}
		return new Variable(Boolean.class, (boolean)this.value || (boolean)other.value);
	}
	
	public Variable equal(Variable other) {
		return new Variable(Boolean.class, this.value.equals(other.value));
	}
	
	public Variable notEqual(Variable other) {
		return new Variable(Boolean.class, !this.value.equals(other.value));
	}
	
	public Variable lessThanOrEqual(Variable other) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((other.type != Integer.class) && (other.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> castTo = type == other.type ? type : Float.class;
		if(castTo == Integer.class) {
			return new Variable(Boolean.class, (int)this.value <= (int)other.value);
		} else {
			return new Variable(Boolean.class, (float)this.value <= (float)other.value);
		}
	}
	
	public Variable greaterThanOrEqual(Variable other) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((other.type != Integer.class) && (other.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> castTo = type == other.type ? type : Float.class;
		if(castTo == Integer.class) {
			return new Variable(Boolean.class, (int)this.value >= (int)other.value);
		} else {
			return new Variable(Boolean.class, (float)this.value >= (float)other.value);
		}
	}
	
	public Variable lessThan(Variable other) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((other.type != Integer.class) && (other.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> castTo = type == other.type ? type : Float.class;
		if(castTo == Integer.class) {
			return new Variable(Boolean.class, (int)this.value < (int)other.value);
		} else {
			return new Variable(Boolean.class, (float)this.value < (float)other.value);
		}
	}
	
	public Variable greaterThan(Variable other) throws InvalidOperationException {
		if(((type != Integer.class) && (type != Float.class)) || ((other.type != Integer.class) && (other.type != Float.class))) {
			throw new InvalidOperationException();
		}
		Class<?> castTo = type == other.type ? type : Float.class;
		if(castTo == Integer.class) {
			return new Variable(Boolean.class, (int)this.value > (int)other.value);
		} else {
			return new Variable(Boolean.class, (float)this.value > (float)other.value);
		}
	}
	
	public void increment() throws InvalidOperationException {
		setValue(add(new Variable(Integer.class, 1)).getValue());
	}
	
	public void decrement() throws InvalidOperationException {
		setValue(subtract(new Variable(Integer.class, 1)).getValue());
	}
	
}


class TypePair {
	public Class<?> first;
	public Class<?> second;
	public TypePair(Class<?> first, Class<?> second) {
		this.first = first;
		this.second = second;
	}
	@Override
	public int hashCode() {
		return first.hashCode() ^ second.hashCode();
	}
	@Override
	public boolean equals(Object other) {
		if (other.getClass() != TypePair.class) return false;
		TypePair otherPair = (TypePair)other;
		return (first == otherPair.first) && (second == otherPair.second);
	}
}