package bce.jni.test;

import java.util.Arrays;

import org.junit.Test;

import bce.jni.bce.BCELibrary;
import bce.jni.utils.BCEUtils;

public class TestBCENative {

	@Test
	public void test() {
		byte[] privateKey = new byte[BCEConstraints.PRIVATE_KEY_LENGTH];
		byte[][] userKeys = new byte[BCEConstraints.PRIVATE_KEY_GEN_BATCH_SIZE][BCEConstraints.USER_PRIVATE_KEY_SIZE];
		byte[][] userKeys1 = new byte[BCEConstraints.PRIVATE_KEY_GEN_BATCH_SIZE][BCEConstraints.USER_PRIVATE_KEY_SIZE];
		
		byte[] CT_C0 = new byte[BCEConstraints.CT_C0_LENGTH];
		byte[] CT_C1 = new byte[BCEConstraints.CT_C1_LENGTH];
		byte[] symmetricKey = new byte[BCEConstraints.SYMMETRIC_KEY_LENGTH];
		byte[] symmetricKey1 = new byte[BCEConstraints.SYMMETRIC_KEY_LENGTH];
		byte[] decrProdBatch = new byte[BCEConstraints.CHANGE_DECR_PROD_BATCH_SIZE * BCEConstraints.PRK_DECR_PROD_LENGTH];
		byte[] decrProdBatch_out = new byte[BCEConstraints.CHANGE_DECR_PROD_BATCH_SIZE * BCEConstraints.PRK_DECR_PROD_LENGTH];
		
		for (int i = 0; i < userKeys.length; i++) {
			userKeys[i] = new byte[BCEConstraints.USER_PRIVATE_KEY_SIZE];
			userKeys1[i] = new byte[BCEConstraints.USER_PRIVATE_KEY_SIZE];
		}
		int retval = BCELibrary.setup(BCEConstraints.CURVE_FILE_NAME.getBytes(), BCEConstraints.USER_NUMBER, BCEConstraints.SYS_PARAMS_FILE_NAME.getBytes(), BCEConstraints.GLOBAL_PARAMS_FILE_NAME.getBytes(), privateKey);
		System.out.println("setup returns: " + retval);
		BCEUtils.printHex(privateKey);
		
		retval = BCELibrary.genPrivateKeys(BCEConstraints.SYS_PARAMS_FILE_NAME.getBytes(), privateKey, BCEConstraints.USER_NUMBER, 1 + 64 * 0, BCEConstraints.PRIVATE_KEY_GEN_BATCH_SIZE, userKeys);
		BCELibrary.genPrivateKeys(BCEConstraints.SYS_PARAMS_FILE_NAME.getBytes(), privateKey, BCEConstraints.USER_NUMBER, 1 + 64 * 1, BCEConstraints.PRIVATE_KEY_GEN_BATCH_SIZE, userKeys1);
		System.out.println("genPrivateKeys returns: " + retval);
		byte[] src = new byte[4];
		System.arraycopy(userKeys1[63], 0, src, 0, 4);
		System.out.println(BCEUtils.bytesToInt(src));

		retval = BCELibrary.encrypt(BCEConstraints.SYS_PARAMS_FILE_NAME.getBytes(), CT_C0, CT_C1, symmetricKey);
		System.out.println("encrypt returns: " + retval);
		
		retval = BCELibrary.decrypt(BCEConstraints.GLOBAL_PARAMS_FILE_NAME.getBytes(), userKeys[16], CT_C0, CT_C1, symmetricKey1);
		System.out.println("decrypt returns: " + retval);
		
		int[] adds = { 5, 6, 7, 10, 11, 12, 13, 14, 15, 16 };
		int nAdds = 10;
		int[] rems = { 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 18, 19 };
		int nRems = 13;
		
		retval = BCELibrary.changeEncryptionProduct(BCEConstraints.SYS_PARAMS_FILE_NAME.getBytes(), adds, nAdds, rems, nRems);
		System.out.println("changeEncryptionProduct returns: " + retval);
		
		for (int i = 0; i < BCEConstraints.CHANGE_DECR_PROD_BATCH_SIZE; i++) {
			System.arraycopy(userKeys[i], BCEConstraints.USER_PRIVATE_KEY_SIZE - BCEConstraints.PRK_DECR_PROD_LENGTH, decrProdBatch, i * BCEConstraints.PRK_DECR_PROD_LENGTH, BCEConstraints.PRK_DECR_PROD_LENGTH);
		}
		retval = BCELibrary.changeDecryptionProduct(BCEConstraints.GLOBAL_PARAMS_FILE_NAME.getBytes(), 1, 64, adds, nAdds, rems, nRems, decrProdBatch, decrProdBatch_out);
		System.out.println("changeDecryptionProduct returns: " + retval);
		
		for (int i = 0; i < BCEConstraints.CHANGE_DECR_PROD_BATCH_SIZE; i++) {
			System.arraycopy(decrProdBatch_out, i * BCEConstraints.PRK_DECR_PROD_LENGTH, userKeys[i], BCEConstraints.USER_PRIVATE_KEY_SIZE - BCEConstraints.PRK_DECR_PROD_LENGTH, BCEConstraints.PRK_DECR_PROD_LENGTH);
		}
		
		Arrays.fill(CT_C0, (byte) 0);
		Arrays.fill(CT_C1, (byte) 0);
		Arrays.fill(symmetricKey, (byte) 0);
		Arrays.fill(symmetricKey1, (byte) 0);
		retval = BCELibrary.encrypt(BCEConstraints.SYS_PARAMS_FILE_NAME.getBytes(), CT_C0, CT_C1, symmetricKey);
		System.out.println("encrypt1 returns: " + retval);
		
		retval = BCELibrary.decrypt(BCEConstraints.GLOBAL_PARAMS_FILE_NAME.getBytes(), userKeys[16], CT_C0, CT_C1, symmetricKey1);
		System.out.println("decrypt1 returns: " + retval);
	}

}
