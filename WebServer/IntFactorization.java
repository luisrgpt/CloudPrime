import java.math.BigInteger;
import java.util.ArrayList;
import java.io.IOException;

public class IntFactorization {

  private BigInteger zero = new BigInteger("0");
  private BigInteger one = new BigInteger("1");
  private BigInteger divisor = new BigInteger("2");
  private ArrayList<BigInteger> factors = new ArrayList<BigInteger>();


  ArrayList<BigInteger>  calcPrimeFactors(BigInteger num) {
 
    if (num.compareTo(one)==0) {
      return factors;
    }

    while(num.remainder(divisor).compareTo(zero)!=0) {
      divisor = divisor.add(one);
    }

    factors.add(divisor);
    return calcPrimeFactors(num.divide(divisor));
  }


  public static String getResponse(String[] args) {
    String res = "";
    IntFactorization obj = new IntFactorization();
    int i = 0;

    ArrayList<BigInteger> factors = 
      obj.calcPrimeFactors(new BigInteger(args[0]));

    res += "The prime factors of " + args[0] + " are ";
    for (BigInteger bi: factors) {
      i++;
      res += bi.toString();
      if (i == factors.size()) {
        res += ".\n";
      } else {
        res += ", ";
      }
    }
    res += "\n";
    return res;
  }
}
