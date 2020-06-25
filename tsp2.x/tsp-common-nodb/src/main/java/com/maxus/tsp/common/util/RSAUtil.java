package com.maxus.tsp.common.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class RSAUtil {

	private static final String KEY_ALGORITHM = "RSA";

	private static final String PUBLIC_KEY = "publicKey";

	private static final String PRIVATE_KEY = "privateKey";

	private static final String PKCS1_PRIVATE_PEM = "/pkcs1-private.pem";

	private static final String PKCS1_PUBLIC_PEM = "/pkcs1-public.pem";

	/** RSA密钥长度必须是64的倍数，在512~65536之间。默认是1024 */
	private static final int KEY_SIZE = 2048;

	/**
	 * 获取公私钥对
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> generateKey() throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(KEY_SIZE);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String, Object> keyMap = new HashMap<String, Object>(2);
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}

	/**
	 * 生成公私钥文件
	 * 
	 * @param filePath
	 *            公私钥文件存储目录
	 * @throws Exception
	 */
	public static void savePemPkcs1(String filePath) throws Exception {
		Map<String, Object> keyMap = generateKey();
		// ----private
		PrivateKey priv = (PrivateKey) keyMap.get(PRIVATE_KEY);
		byte[] privBytes = priv.getEncoded();

		PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
		ASN1Encodable encodable = pkInfo.parsePrivateKey();
		ASN1Primitive primitive = encodable.toASN1Primitive();
		byte[] privateKeyPKCS1 = primitive.getEncoded();

		// pkcs1
		PemObject pemObject = new PemObject("RSA PRIVATE KEY", privateKeyPKCS1);
		PemWriter writer = new PemWriter(new FileWriter(filePath + PKCS1_PRIVATE_PEM));
		writer.writeObject(pemObject);
		writer.close();

		// ----public
		PublicKey pub = (PublicKey) keyMap.get(PUBLIC_KEY);
		byte[] pubBytes = pub.getEncoded();

		SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(pubBytes);
		primitive = spkInfo.parsePublicKey();
		byte[] publicKeyPKCS1 = primitive.getEncoded();

		pemObject = new PemObject("RSA PUBLIC KEY", publicKeyPKCS1);
		PemWriter pemWriter = new PemWriter(new FileWriter(filePath + PKCS1_PUBLIC_PEM));
		pemWriter.writeObject(pemObject);
		pemWriter.close();
	}

	/**
	 * 
	 * @Title: publicKeyEncrypt
	 * @Description: 公钥加密
	 * @param: @param
	 *             publicKeyFile
	 * @param: @param
	 *             data
	 * @param: @return
	 * @param: @throws
	 *             IOException
	 * @param: @throws
	 *             NoSuchAlgorithmException
	 * @param: @throws
	 *             InvalidKeySpecException
	 * @param: @throws
	 *             CertificateException
	 * @param: @throws
	 *             InvalidCipherTextException
	 * @return: byte[] 密文
	 * @throws @author
	 *             zekym
	 * @Date 2017年7月23日 上午7:57:59
	 */
	public static byte[] publicKeyEncrypt(File publicKeyFile, byte[] data) throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException, CertificateException, InvalidCipherTextException {
		PEMParser pemParser = new PEMParser(new FileReader(publicKeyFile));
		SubjectPublicKeyInfo object = (SubjectPublicKeyInfo) pemParser.readObject();
		pemParser.close();
		AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(object);
		AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
		engine.init(true, publicKey);
		int i = 0;
		List<Byte> resultList = new ArrayList<Byte>();
		int inputSize = engine.getInputBlockSize();
		while (data.length - i * inputSize > 0) {
			if (data.length - i * inputSize > inputSize) {
				byte[] ed = engine.processBlock(data, i * inputSize, inputSize);
				for (int j = 0; j < ed.length; j++) {
					resultList.add(ed[j]);
				}
			} else {
				byte[] ed = engine.processBlock(data, i * inputSize, data.length - i * inputSize);
				for (int j = 0; j < ed.length; j++) {
					resultList.add(ed[j]);
				}
			}
			i++;
		}
		byte[] result = new byte[resultList.size()];
		for (int m = 0; m < resultList.size(); m++) {
			result[m] = resultList.get(m);
		}
		return result;
	}

	public static byte[] publicKeyEncrypt(String publicKeyStr, byte[] data) throws IOException,
			NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, InvalidCipherTextException {
		PEMParser pemParser = new PEMParser(new StringReader(publicKeyStr));
		SubjectPublicKeyInfo object = (SubjectPublicKeyInfo) pemParser.readObject();
		pemParser.close();
		AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(object);
		AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
		engine.init(true, publicKey);
		int i = 0;
		List<Byte> resultList = new ArrayList<Byte>();
		int inputSize = engine.getInputBlockSize();
		while (data.length - i * inputSize > 0) {
			if (data.length - i * inputSize > inputSize) {
				byte[] ed = engine.processBlock(data, i * inputSize, inputSize);
				for (int j = 0; j < ed.length; j++) {
					resultList.add(ed[j]);
				}
			} else {
				byte[] ed = engine.processBlock(data, i * inputSize, data.length - i * inputSize);
				for (int j = 0; j < ed.length; j++) {
					resultList.add(ed[j]);
				}
			}
			i++;
		}
		byte[] result = new byte[resultList.size()];
		for (int m = 0; m < resultList.size(); m++) {
			result[m] = resultList.get(m);
		}
		return result;
	}

	public static byte[] privateKeyDecrypt(File privateKeyFile, byte[] encryptedData) throws Exception {
		PrivateKey privateKey = getPrivateKey(privateKeyFile);
		byte[] privBytes = privateKey.getEncoded();

		PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
		AsymmetricKeyParameter anotherprivateKey = PrivateKeyFactory.createKey(pkInfo);
		AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
		engine.init(false, anotherprivateKey);
		int inputSize = engine.getInputBlockSize();
		int i = 0;
		List<Byte> resultList = new ArrayList<Byte>();
		while (encryptedData.length - i * inputSize > 0) {
			if (encryptedData.length - i * inputSize > inputSize) {
				byte[] data = engine.processBlock(encryptedData, i * inputSize, inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			} else {
				byte[] data = engine.processBlock(encryptedData, i * inputSize, encryptedData.length - i * inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			}
			i++;
		}
		byte[] result = new byte[resultList.size()];
		for (int m = 0; m < resultList.size(); m++) {
			result[m] = resultList.get(m);
		}
		return result;
	}

	public static byte[] privateKeyDecrypt(String privateKeyStr, byte[] encryptedData) throws Exception {

		PrivateKey privateKey = getPrivateKey(privateKeyStr);
		byte[] privBytes = privateKey.getEncoded();

		PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
		AsymmetricKeyParameter anotherprivateKey = PrivateKeyFactory.createKey(pkInfo);
		AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
		engine.init(false, anotherprivateKey);
		int inputSize = engine.getInputBlockSize();
		int i = 0;
		List<Byte> resultList = new ArrayList<Byte>();
		while (encryptedData.length - i * inputSize > 0) {
			if (encryptedData.length - i * inputSize > inputSize) {
				byte[] data = engine.processBlock(encryptedData, i * inputSize, inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			} else {
				byte[] data = engine.processBlock(encryptedData, i * inputSize, encryptedData.length - i * inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			}
			i++;
		}
		byte[] result = new byte[resultList.size()];
		for (int m = 0; m < resultList.size(); m++) {
			result[m] = resultList.get(m);
		}
		return result;
	}

	public static byte[] publicKeyDecrypt(File publicKeyFile, byte[] encryptedData) throws IOException,
			NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, InvalidCipherTextException {
		PEMParser pemParser = new PEMParser(new FileReader(publicKeyFile));
		SubjectPublicKeyInfo object = (SubjectPublicKeyInfo) pemParser.readObject();
		pemParser.close();
		AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(object);
		AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
		engine.init(false, publicKey);
		int inputSize = engine.getInputBlockSize();
		int i = 0;
		List<Byte> resultList = new ArrayList<Byte>();
		while (encryptedData.length - i * inputSize > 0) {
			if (encryptedData.length - i * inputSize > inputSize) {
				byte[] data = engine.processBlock(encryptedData, i * inputSize, inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			} else {
				byte[] data = engine.processBlock(encryptedData, i * inputSize, encryptedData.length - i * inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			}
			i++;
		}
		byte[] result = new byte[resultList.size()];
		for (int m = 0; m < resultList.size(); m++) {
			result[m] = resultList.get(m);
		}
		return result;
	}

	public static byte[] publicKeyDecrypt(String publicKeyStr, byte[] encryptedData) throws IOException,
			NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, InvalidCipherTextException {
		PEMParser pemParser = new PEMParser(new StringReader(publicKeyStr));
		SubjectPublicKeyInfo object = (SubjectPublicKeyInfo) pemParser.readObject();
		pemParser.close();
		AsymmetricKeyParameter publicKey = PublicKeyFactory.createKey(object);
		AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
		engine.init(false, publicKey);
		int inputSize = engine.getInputBlockSize();
		int i = 0;
		List<Byte> resultList = new ArrayList<Byte>();
		while (encryptedData.length - i * inputSize > 0) {
			if (encryptedData.length - i * inputSize > inputSize) {
				byte[] data = engine.processBlock(encryptedData, i * inputSize, inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			} else {
				byte[] data = engine.processBlock(encryptedData, i * inputSize, encryptedData.length - i * inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			}
			i++;
		}
		byte[] result = new byte[resultList.size()];
		for (int m = 0; m < resultList.size(); m++) {
			result[m] = resultList.get(m);
		}
		return result;
	}

	public static byte[] privateKeyEncrypt(File privateKeyFile, byte[] plainData) throws Exception {
		PrivateKey privateKey = getPrivateKey(privateKeyFile);
		byte[] privBytes = privateKey.getEncoded();

		PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
		AsymmetricKeyParameter anotherprivateKey = PrivateKeyFactory.createKey(pkInfo);
		AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
		engine.init(true, anotherprivateKey);
		int i = 0;
		int inputSize = engine.getInputBlockSize();
		List<Byte> resultList = new ArrayList<Byte>();
		while (plainData.length - i * inputSize > 0) {
			if (plainData.length - i * inputSize > inputSize) {
				byte[] data = engine.processBlock(plainData, i * inputSize, inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			} else {
				byte[] data = engine.processBlock(plainData, i * inputSize, plainData.length - i * inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			}
			i++;
		}
		byte[] result = new byte[resultList.size()];
		for (int m = 0; m < resultList.size(); m++) {
			result[m] = resultList.get(m);
		}
		return result;
	}

	public static byte[] privateKeyEncrypt(String privateKeyStr, byte[] plainData) throws Exception {
		PrivateKey privateKey = getPrivateKey(privateKeyStr);
		byte[] privBytes = privateKey.getEncoded();

		PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
		AsymmetricKeyParameter anotherprivateKey = PrivateKeyFactory.createKey(pkInfo);
		AsymmetricBlockCipher engine = new PKCS1Encoding(new RSAEngine());
		engine.init(true, anotherprivateKey);
		int i = 0;
		int inputSize = engine.getInputBlockSize();
		List<Byte> resultList = new ArrayList<Byte>();
		while (plainData.length - i * inputSize > 0) {
			if (plainData.length - i * inputSize > inputSize) {
				byte[] data = engine.processBlock(plainData, i * inputSize, inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			} else {
				byte[] data = engine.processBlock(plainData, i * inputSize, plainData.length - i * inputSize);
				for (int j = 0; j < data.length; j++) {
					resultList.add(data[j]);
				}
			}
			i++;
		}
		byte[] result = new byte[resultList.size()];
		for (int m = 0; m < resultList.size(); m++) {
			result[m] = resultList.get(m);
		}
		return result;
	}

	/**
	 * 获取私钥
	 * 
	 * @param privateKeyFile
	 *            私钥文件
	 * @return 私钥
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static PrivateKey getPrivateKey(File privateKeyFile)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile));
		Object object = pemParser.readObject();
		pemParser.close();
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());
		KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
		return kp.getPrivate();
	}

	public static PrivateKey getPrivateKey(String privateKeyFile)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		PEMParser pemParser = new PEMParser(new StringReader(privateKeyFile));
		Object object = pemParser.readObject();
		pemParser.close();
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());
		KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
		return kp.getPrivate();
	}
	
	/**
	 * 先tbox私钥加密加tsp公钥加密
	 * @param context
	 * @return
	 */
	public static String getTboxRSA(String context, String tbox_private_pem, String tsp_public_pem) {
		String res= null;
		String plainText = context;
		System.out.println("明文: " + plainText);
		System.out.println("私钥加密，公钥加密。。。。。");
		byte[] data1;
		try {
			System.out.print("明文字节流"+DatatypeConverter.parseHexBinary(plainText));
			data1 = privateKeyEncrypt(new File(tbox_private_pem), DatatypeConverter.parseHexBinary(plainText));
		
		String s1 = DatatypeConverter.printHexBinary(data1);
			
		System.out.println("私钥加密后的字符串: " + s1);
		System.out.println("私钥加密后的字符串长度: " + s1.length());
		byte[] d2 = publicKeyEncrypt(new File(tsp_public_pem), data1);
		String encryptedStr2 = DatatypeConverter.printHexBinary(d2);
		res = encryptedStr2;
		System.out.println("公钥加密后的字符串: " + encryptedStr2);
		System.out.println("公钥加密后的字符串长度: " + encryptedStr2.length());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

//	public static void main(String[] args) throws Exception {
////		savePemPkcs1("d:/test/0801");
//		test();
////		test1();
//	}

//	public static void test() throws Exception {
		// savePemPkcs1("d:/test");
		// String plainText =
		// "this is a test text.this is a test text.this is a test text.this is
		// a test text.this is a test text.this is a test text.this is a test
		// text.this is a test text.this is a test text.this is a test text.this
		// is a test text.this is a test text.this is a test text.this is a test
		// text.this is a test text.this is a test text.this is a test text.this
		// is a test text.this is a test text.this is a test text.this is a test
		// text.";
//		String plainText = "2323003F000FAFAFA20170101020202000001001000001000000020021000000000012345600000000000000000000001234567890010107E10518090F1B2C";
//		byte[] d22 = DatatypeConverter.parseHexBinary(plainText);
//		System.out.println("明文: " + plainText);
//		System.out.println("私钥加密，公钥加密。。。。。");
//		byte[] data1 = privateKeyEncrypt(new File("d:/test/0801/pkcs1-private.pem"), d22);
//		String s1 = DatatypeConverter.printHexBinary(data1);
//		System.out.println("私钥加密后的字符串: " + s1);
//		System.out.println("私钥加密后的字符串长度: " + s1.length());
//		byte[] d2 = publicKeyEncrypt(new File("d:/test/0801/pkcs1-public.pem"), data1);
//		String encryptedStr2 = DatatypeConverter.printHexBinary(d2);
//		System.out.println("公钥加密后的字符串: " + encryptedStr2);
//		System.out.println("公钥加密后的字符串长度: " + encryptedStr2.length());
//		String k = "583a785324d397eb6041b510ae93592dd093dff605f45ba3516e383c81f753c394fc01ffa30e88a3eab8745dd5c364e675e0961ae605d7049a681c4088ac21a811a65bdb9882af4e7958e2de4af3cf33d6aa5fe8e4158dc2768025047d98e4a512f2a95012564e0d6530c6d6e6a9af18d6877c517d948acea17a1d7b8a17222b9f2df14c683d445f83eda23c0dd838f43391005b79cb317ef4972f8d4c03c7f654a76f4559f9df46cac4873abe129e3f1f9c7820920fbd821e031436744fe95f5e8ce59f58973541159eb9a1a44f86adf979c26a9126bf92299a609dbc5ebebec28dd1c176c5177ce2bb13cdcb1c91bc63ffbbbeaa629c2770b8fb24e41bde11358d30de25cedafeac74afcdb9a24d938983b6ad8ab1f967f3c7c36146b0b3f767670dc658dd9d35f50c1ce5431854823fbaf3798f3ed70f79dea0341e4da364e5f141f10697ece50ac37898e5dfd82991c48ea4e94e3f0688626955e22b4dfde3801b61c26b5b9f9df19a843b1354e1e1909f37d17fa2d24c5fc058462801337427b66808c6c4f70e0c5bbf2ba5245f8475773bd93fff0b4949f83ce406c6451642aa4c40e0f22a20c1d6dc60c159f4a7e439626f766b8ea6bc7020b99ded938c2af840238cb2635a88a3b49a659ec576b4bcebeee0d89be077757397152c181174b4a867db242ec0da3b9d67e504b9fbf1fd886129985e0d3f7c7d32764ba8";
//		k = k.replace("-", "");
//		d2 = DatatypeConverter.parseHexBinary(k);
//		d2 = DatatypeConverter.parseHexBinary(encryptedStr2);
//		System.out.println("\n私钥解密，公钥解密。。。。。");
//		byte[] data3 = privateKeyDecrypt(new File("d:/test/0801/pkcs1-private.pem"), d2);
//		String encryptedStr3 = DatatypeConverter.printHexBinary(data3);
//		System.out.println("私钥解密后的字符串: " + encryptedStr3);
//		byte[] a4 = publicKeyDecrypt(new File("d:/test/0801/pkcs1-public.pem"), data3);
//		System.out.println("公钥解密后的字符串: " + DatatypeConverter.printHexBinary(a4));
//		System.out.println("公钥解密后的字符串: " + new String(a4));
//	}

//	public static void test1() throws Exception {
//		String plainText = "583a785324d397eb6041b510ae93592dd093dff605f45ba3516e383c81f753c394fc01ffa30e88a3eab8745dd5c364e675e0961ae605d7049a681c4088ac21a811a65bdb9882af4e7958e2de4af3cf33d6aa5fe8e4158dc2768025047d98e4a512f2a95012564e0d6530c6d6e6a9af18d6877c517d948acea17a1d7b8a17222b9f2df14c683d445f83eda23c0dd838f43391005b79cb317ef4972f8d4c03c7f654a76f4559f9df46cac4873abe129e3f1f9c7820920fbd821e031436744fe95f5e8ce59f58973541159eb9a1a44f86adf979c26a9126bf92299a609dbc5ebebec28dd1c176c5177ce2bb13cdcb1c91bc63ffbbbeaa629c2770b8fb24e41bde11358d30de25cedafeac74afcdb9a24d938983b6ad8ab1f967f3c7c36146b0b3f767670dc658dd9d35f50c1ce5431854823fbaf3798f3ed70f79dea0341e4da364e5f141f10697ece50ac37898e5dfd82991c48ea4e94e3f0688626955e22b4dfde3801b61c26b5b9f9df19a843b1354e1e1909f37d17fa2d24c5fc058462801337427b66808c6c4f70e0c5bbf2ba5245f8475773bd93fff0b4949f83ce406c6451642aa4c40e0f22a20c1d6dc60c159f4a7e439626f766b8ea6bc7020b99ded938c2af840238cb2635a88a3b49a659ec576b4bcebeee0d89be077757397152c181174b4a867db242ec0da3b9d67e504b9fbf1fd886129985e0d3f7c7d32764ba8";
//		byte[] text = null;
//		System.out.println("明文: " + plainText);
//		System.out.println("私钥加密，公钥加密。。。。。");
//		byte[] data1 = privateKeyEncrypt(readFileByLines("d:/test/frommail/tbox_private_key.pem"),
//				plainText.getBytes());
//		String s1 = DatatypeConverter.printHexBinary(data1);
//		System.out.println("私钥加密后的字符串: " + s1);
//		System.out.println("私钥加密后的字符串长度: " + s1.length());
//		byte[] d2 = publicKeyEncrypt(readFileByLines("d:/test/frommail/pkcs1-public.pem"), data1);
//		String encryptedStr2 = DatatypeConverter.printHexBinary(d2);
//		System.out.println("公钥加密后的字符串: " + encryptedStr2);
//		System.out.println("公钥加密后的字符串长度: " + encryptedStr2.length());
//		String k = "583a785324d397eb6041b510ae93592dd093dff605f45ba3516e383c81f753c394fc01ffa30e88a3eab8745dd5c364e675e0961ae605d7049a681c4088ac21a811a65bdb9882af4e7958e2de4af3cf33d6aa5fe8e4158dc2768025047d98e4a512f2a95012564e0d6530c6d6e6a9af18d6877c517d948acea17a1d7b8a17222b9f2df14c683d445f83eda23c0dd838f43391005b79cb317ef4972f8d4c03c7f654a76f4559f9df46cac4873abe129e3f1f9c7820920fbd821e031436744fe95f5e8ce59f58973541159eb9a1a44f86adf979c26a9126bf92299a609dbc5ebebec28dd1c176c5177ce2bb13cdcb1c91bc63ffbbbeaa629c2770b8fb24e41bde11358d30de25cedafeac74afcdb9a24d938983b6ad8ab1f967f3c7c36146b0b3f767670dc658dd9d35f50c1ce5431854823fbaf3798f3ed70f79dea0341e4da364e5f141f10697ece50ac37898e5dfd82991c48ea4e94e3f0688626955e22b4dfde3801b61c26b5b9f9df19a843b1354e1e1909f37d17fa2d24c5fc058462801337427b66808c6c4f70e0c5bbf2ba5245f8475773bd93fff0b4949f83ce406c6451642aa4c40e0f22a20c1d6dc60c159f4a7e439626f766b8ea6bc7020b99ded938c2af840238cb2635a88a3b49a659ec576b4bcebeee0d89be077757397152c181174b4a867db242ec0da3b9d67e504b9fbf1fd886129985e0d3f7c7d32764ba8";
//
//		// String k =
//		// "C9-23-55-F4-ED-62-AD-2F-73-43-EC-5F-A5-A5-21-DD-4A-43-E0-32-D9-B8-1F-3F-5B-EC-7E-70-72-8F-B6-EF-01-A5-A3-E2-EC-BB-BA-FD-50-7B-79-95-5F-1E-11-3D-03-70-80-3C-EC-A9-D8-CE-86-49-1A-9F-F3-15-42-5E-E9-01-B5-0A-14-DA-69-6E-0E-20-F3-CE-F3-54-A9-75-E0-88-FF-5C-C7-DB-96-E5-EF-B0-C5-26-79-D8-5A-8F-DC-96-75-EF-4B-4D-D1-BE-8F-4B-58-EE-A9-BD-12-BF-04-F4-C2-6F-D3-DC-38-2B-55-32-65-94-E3-E5-59-03-FB-8A-7D-2C-52-50-5D-51-99-4D-8A-AF-AC-15-E0-66-56-86-C3-3C-21-C4-E0-59-F3-99-BC-50-3A-2F-74-1F-50-F0-D4-76-2B-B8-7B-75-F2-41-CD-27-96-13-33-BB-D3-72-8D-95-6F-68-96-75-D8-DF-A1-DF-2F-63-CD-BB-20-B3-E7-63-E1-62-92-58-8E-ED-66-E7-3F-80-17-4D-C4-80-E9-85-42-E9-36-8E-1C-C4-3A-34-07-3F-0D-E1-6E-48-33-29-F7-D7-9E-A4-5B-B1-FE-C5-26-30-67-AA-7F-77-82-DB-2A-1D-62-11-2F-D3-53-60-CA-BF-F1-4B-5A-4F-23-34-07-C9-BD-19-82-EF-55-7E-28-BD-F2-7A-87-5F-00-E9-E0-FD-1F-B6-87-0F-90-EF-4F-83-B8-D4-DC-F8-00-A8-94-A6-B2-BD-52-52-32-0F-01-C8-77-D6-B8-36-D1-A1-7B-D8-A6-0E-F3-FE-14-42-AD-24-0B-5B-41-A7-9B-0F-71-DE-A2-E6-3A-7A-DB-5B-D2-19-BB-8B-D0-2D-59-0C-38-99-8F-31-A5-51-49-0A-81-45-4D-81-FD-A4-FB-A7-BE-3B-77-37-30-36-5E-7C-DB-C5-14-C2-E9-85-9E-ED-86-BD-F2-7A-65-1E-EA-44-34-E6-05-56-EB-C6-79-7C-EE-F5-D4-B5-FF-2D-E8-9A-B6-AA-DD-66-A8-A0-C6-1C-30-BE-BF-CB-75-0F-7B-0A-86-39-CC-65-73-F0-23-45-C0-7D-E3-73-22-62-0A-F7-27-D7-C0-93-43-A6-85-2E-40-35-C8-70-0F-8E-73-49-6F-E4-77-84-74-4A-E6-F1-19-BD-5C-22-BC-D5-BF-54-F3-D7-66-A8-F4-C7-9A-FC-EE-10-32-2F-CA-57-49-61-83-AA-BA-57-27-BC-02-22-83-C5-0C-BB-75-7D-37-07-5A-8E-AF-2F-AF-8C-77-EB-37-42-DB-3F-26-9D-CB-FA-5E-A2-65-A8";
//		// String k =
//		// "598C1BF05684028E8801299DCF481C66D5E77D151CF74E4EB3C7EF6CCCC318A0C09684AF60143A0A0766F9D2C368DB7CA6AD5C4B78E970361462C4573F1D51B377258903F6AE99DBAB018D3A3205D966B87E6471D32684A8A7FEA887DC0BE0BA66CC8FF43800015BB92F0B177C384C7F16B0BAAE4FBA915AAA9842FA440AD691887A9AF695D0A94E2D1A8276349F6655D221DAA3EFEAEB6F0399904BC96C89A222D5201E4F65E974D5F632D613B4F377F4870F27A9E33DFA9D449B7AA5DD285F8A71C93454D97F6F627E338C533A79A28CD02486326CC25EBEBB224ED53A803AC2C3713A28F2D4AEE2C5155F9E525B85A151D55738A94F007C1614AEF5DA37C77C83F79A2EF6FE47A012220445AEA0F8B86944C47A03305A7C65625CA84477028081C7F742AAB3A82C970A08F126813F49EE4F0B538B7252D774DDD79BE2237B8325BEBA6CC52BDF7F0B5350F321B86D54CE2DE1BABEE7147DCB399E6C26BA32859ED6303EC2D5A93C69BB5438DD5532A420DE1F660C85578FB26CEC1B7DC22186F088DEA09E348692C5FDF7B343D25492BBE08A3885F7A797F46645FC2148E1B3BD1A361A03A1B7FD2F42AD0B3C0E2EDC7269173ACEE119D2557007AF46C8726E0274EC56AB1DB81C800A8FF03D4F8DC54F5609D2150A79099DA40962698EE0AD203184C191F24365CADF6B51A7A421D2CE43E2A72EB493171D126195984B8E";
//		k = k.replace("-", "");
////		 d2 = DatatypeConverter.parseHexBinary(k);
//		System.out.println("\n私钥解密，公钥解密。。。。。");
//		byte[] data3 = privateKeyDecrypt(readFileByLines("d:/test/frommail/pkcs1-private.pem"), d2);
//		String encryptedStr3 = DatatypeConverter.printHexBinary(data3);
//		System.out.println("私钥解密后的字符串: " + encryptedStr3);
//		byte[] a4 = publicKeyDecrypt(readFileByLines("d:/test/frommail/tbox_public_key.pem"), data3);
//		System.out.println("公钥解密后的字符串: " + new String(a4));
//	}

	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 * 
	 * @throws IOException
	 */
//	public static String readFileByLines(String fileName) throws IOException {
//		File file = new File(fileName);
//		BufferedReader reader = null;
//		try {
//			reader = new BufferedReader(new FileReader(file));
//			String tmpStr = null;
//			StringBuffer sb = new StringBuffer();
//			// 一次读入一行，直到读入null为文件结束
//			while ((tmpStr = reader.readLine()) != null) {
//				sb.append(tmpStr + System.getProperty("line.separator"));
//			}
//			reader.close();
//			return sb.toString();
//		} finally {
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (IOException e1) {
//				}
//			}
//		}
//	}

}
