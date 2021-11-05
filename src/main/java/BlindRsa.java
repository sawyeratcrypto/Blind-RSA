import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;


public class BlindRsa
{
	static KeyPair alicePair;  //alice key pair

	static RSAPrivateCrtKey alicePrivate; // alice private key d

	static RSAPublicKey alicePublic; // alice public key e

	static BigInteger N; // Key pair's modulus

	static BigInteger mu; //first message Bob sends to Alice, mu = H(msg) * r^e mod N

	static BigInteger muprime;// Alice's message to Bob, mu'=mu^d mod N


	public static void main(String[] args)
	{

		Security.addProvider(new BouncyCastleProvider());
		try
		{
			long start = System.currentTimeMillis(); //get current time in milliseconds

			alicePair = Alice.produceKeyPair(); // call Alice's function to produce a key pair (N, e ,d), and save it in alicePair variable

			alicePrivate = (RSAPrivateCrtKey) alicePair.getPrivate(); //get the private key d out of the key pair Alice produced

			alicePublic = (RSAPublicKey) alicePair.getPublic(); //get  the public key e out of the key pair Alice produced

			N = alicePublic.getModulus(); //get the modulus of the key pair produced by Alice

			mu = Bob.calculateMu(alicePublic); //call Bob's function calculateMu with alice Public key as input in order to calculate mu, and store it in mu variable

			muprime = Alice.calculateMuPrimeWithChineseRemainderTheorem(mu); // call Alice's function calculateMuPrime with mu produced earlier by Bob as input, to calculate  mu' and store it to muprime  variable

			String sig = Bob.signatureCalculation(muprime); // call Bob's function signatureCalculation with muprime as input and calculate the signature, then store it in sig variable

			Bob.verify(sig); //Bob is checking if the signature he got from Alice is valid, that can be easily computed because (m^d)^e modN = m

			System.out.println();
			long elapsedTimeMillis = System.currentTimeMillis() - start;
			System.out.println("Program executed in " + elapsedTimeMillis + " milliseconds");


			// try to blind before the encryption
			Cipher enCipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
			enCipher.init(Cipher.ENCRYPT_MODE, BlindRsa.alicePublic);
			byte[] blindBeforeEncryptText =  Bob.m.multiply(Bob.r).mod(N).toByteArray();
			byte[] ciphertext = enCipher.doFinal(blindBeforeEncryptText);

			Cipher deCipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
			deCipher.init(Cipher.DECRYPT_MODE, BlindRsa.alicePrivate);
			byte[] decrypted = deCipher.doFinal(ciphertext);

			BigInteger unblind = Bob.r.modInverse(N).multiply(new BigInteger(decrypted)).mod(N);

			String recovered = new String(unblind.toByteArray());
			System.out.println("Recovered matches original " + recovered.equals(new String(Bob.m.toByteArray())));

		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
