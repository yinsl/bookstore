package com.maxus.tsp.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class Asn1Util {

	private static Logger logger = LogManager.getLogger(Asn1Util.class);

	public static void main(String[] args) {
		decodeSeq(encodeSeq("abcdefg"));
	}

	public static byte[] decodeSeq(byte[] in) {
		byte[] result = null;
		try {
			ASN1InputStream asn1InputStream = new ASN1InputStream(in);
			ASN1Sequence sequence = (ASN1Sequence) asn1InputStream.readObject();
			DERTaggedObject taggedObj = (DERTaggedObject) sequence.parser().readObject().toASN1Primitive();
			DEROctetString derOctetString = (DEROctetString) taggedObj.getObject();
			result = derOctetString.getOctets();
			asn1InputStream.close();
		} catch (Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		return result;
	}

	public static byte[] encodeSeq(String in) {
		return encodeSeq(in.getBytes());
	}
	
	public static byte[] encodeSeq(byte[] in) {
		byte[] result = null;
		try {
			ASN1EncodableVector encodableVector = new ASN1EncodableVector();
			encodableVector.add(new DERTaggedObject(false, 0, new DEROctetString(in)));
			DERSequence derSequence = new DERSequence(encodableVector);
			result = derSequence.getEncoded(ASN1Encoding.DER);
		} catch (Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		return result;
	}

}
