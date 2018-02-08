package com.sunline;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

public class PdfSign {
	
	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) {
		String keyStore = "D://����//wspaceSVN//PdfSign//ca.p12";
		String password = "123456";// keystory����
		String src = "D://����//wspaceSVN//PdfSign//111.pdf";// ԭʼpdf
		String dest = "D://����//wspaceSVN//PdfSign//demo_signed.pdf";// ǩ����ɵ�pdf
		String chapterPath = "D://����//wspaceSVN//PdfSign//sign.png";// ǩ��ͼƬ
		String reason = "����ǩ��";
		String location = "����";
		String creator = "�����Ƽ�";
		String contact = "���ڳ����Ƽ�";
		
		try {
			PdfSign.sign(src, dest, keyStore,password, reason, 
					location, creator, contact, chapterPath, 1);
			
			
			PdfSign.sign(dest, "D://����//wspaceSVN//PdfSign//222.pdf", keyStore,password, reason, 
					location, creator, contact, chapterPath, 3);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void sign(String srcPdfFile, String destPdfFile, String p12File,
			String password, String reason, String location, String creator, 
			String contact, String chapterPath, int page) throws Exception {
		InputStream p12Stream = null;
		InputStream srcPdfStream = null;
		OutputStream destPdfStream = null;
		PdfReader reader = null;
		PdfStamper stamper = null;
		try {
			p12Stream = new FileInputStream(p12File);
		 	srcPdfStream = new FileInputStream(srcPdfFile);
		 	destPdfStream = new FileOutputStream(destPdfFile);
		 	// ��ȡͼ��ͼƬ�����image��itext����image
 			Image image = Image.getInstance(chapterPath);
		 	// Creating the reader and the stamper����ʼpdfreader
 			reader = new PdfReader(srcPdfStream);
 			// Ŀ���ļ������
			// ����ǩ�¹���PdfStamper �����һ��boolean����
			// false�Ļ���pdf�ļ�ֻ����ǩ��һ�Σ����ǩ�������һ����Ч
			// true�Ļ���pdf���Ա�׷��ǩ������ǩ���߿���ʶ���ÿ��ǩ��֮���ĵ��Ƿ��޸�
			stamper = PdfStamper.createSignature(reader, destPdfStream, '\0',
					null, true);
			
			// ��ȡkeystore �����˽Կ��֤����
			KeyStore ks = KeyStore.getInstance("PKCS12");
			char[] pwd = password.toCharArray();
			ks.load(p12Stream, pwd);
			String alias = (String) ks.aliases().nextElement();
			PrivateKey pk = (PrivateKey) ks.getKey(alias, pwd);
			Certificate[] chain = ks.getCertificateChain(alias);
			
			// ��ȡ����ǩ�����Զ����趨����ǩ�µ�����
			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
			appearance.setReason(reason);
			appearance.setLocation(location);
			appearance.setSignatureCreator(creator);
			appearance.setContact(contact);
			// ����ǩ����λ�ã�ҳ�룬ǩ�������ƣ����׷��ǩ����ʱ��ǩ�������Ʋ���һ��
			// ǩ����λ�ã���ͼ�������pdfҳ���λ�����꣬ԭ��Ϊpdfҳ�����½�
			// �ĸ������ķֱ��ǣ�ͼ�����½�x��ͼ�����½�y��ͼ�����Ͻ�x��ͼ�����Ͻ�y
			appearance.setVisibleSignature(new Rectangle(200, 200, 300, 300), page,
					"sig1"+page);
			
			appearance.setSignatureGraphic(image);
			appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
			// ����ͼ�µ���ʾ��ʽ������ѡ�����ֻ��ʾͼ�£�����������ģʽ������ͼ�º�ǩ������һͬ��ʾ��
			appearance.setRenderingMode(RenderingMode.GRAPHIC);
			
			// �����itext�ṩ��2������ǩ���Ľӿڣ������Լ�ʵ�֣��������˵���ʵ��
			// ժҪ�㷨
			ExternalDigest digest = new BouncyCastleDigest();
			// ǩ���㷨
			ExternalSignature signature = new PrivateKeySignature(pk,
					DigestAlgorithms.SHA1, null);
			// ����itextǩ���������pdfǩ��CryptoStandard.CMS ǩ����ʽ�������������
			MakeSignature.signDetached(appearance, digest, signature, chain, null,
					null, null, 0, CryptoStandard.CMS);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			try {
				if (stamper != null) {
					stamper.close();
				}
				if (reader != null) {
					reader.close();
				}
				if (p12Stream != null) {
					p12Stream.close();
				}
				if (srcPdfStream != null) {
					srcPdfStream.close();
				}
				if (destPdfStream != null) {
					destPdfStream.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				throw ex;
			}
		}
	}
	
	private static void pageSign() {
		
	}
}
