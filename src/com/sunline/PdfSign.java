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
		String keyStore = "D://深圳//wspaceSVN//PdfSign//ca.p12";
		String password = "123456";// keystory密码
		String src = "D://深圳//wspaceSVN//PdfSign//111.pdf";// 原始pdf
		String dest = "D://深圳//wspaceSVN//PdfSign//demo_signed.pdf";// 签名完成的pdf
		String chapterPath = "D://深圳//wspaceSVN//PdfSign//sign.png";// 签章图片
		String reason = "长亮签署";
		String location = "深圳";
		String creator = "长亮科技";
		String contact = "深圳长亮科技";
		
		try {
			PdfSign.sign(src, dest, keyStore,password, reason, 
					location, creator, contact, chapterPath, 1);
			
			
			PdfSign.sign(dest, "D://深圳//wspaceSVN//PdfSign//222.pdf", keyStore,password, reason, 
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
		 	// 读取图章图片，这个image是itext包的image
 			Image image = Image.getInstance(chapterPath);
		 	// Creating the reader and the stamper，开始pdfreader
 			reader = new PdfReader(srcPdfStream);
 			// 目标文件输出流
			// 创建签章工具PdfStamper ，最后一个boolean参数
			// false的话，pdf文件只允许被签名一次，多次签名，最后一次有效
			// true的话，pdf可以被追加签名，验签工具可以识别出每次签名之后文档是否被修改
			stamper = PdfStamper.createSignature(reader, destPdfStream, '\0',
					null, true);
			
			// 读取keystore ，获得私钥和证书链
			KeyStore ks = KeyStore.getInstance("PKCS12");
			char[] pwd = password.toCharArray();
			ks.load(p12Stream, pwd);
			String alias = (String) ks.aliases().nextElement();
			PrivateKey pk = (PrivateKey) ks.getKey(alias, pwd);
			Certificate[] chain = ks.getCertificateChain(alias);
			
			// 获取数字签章属性对象，设定数字签章的属性
			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
			appearance.setReason(reason);
			appearance.setLocation(location);
			appearance.setSignatureCreator(creator);
			appearance.setContact(contact);
			// 设置签名的位置，页码，签名域名称，多次追加签名的时候，签名域名称不能一样
			// 签名的位置，是图章相对于pdf页面的位置坐标，原点为pdf页面左下角
			// 四个参数的分别是，图章左下角x，图章左下角y，图章右上角x，图章右上角y
			appearance.setVisibleSignature(new Rectangle(200, 200, 300, 300), page,
					"sig1"+page);
			
			appearance.setSignatureGraphic(image);
			appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
			// 设置图章的显示方式，如下选择的是只显示图章（还有其他的模式，可以图章和签名描述一同显示）
			appearance.setRenderingMode(RenderingMode.GRAPHIC);
			
			// 这里的itext提供了2个用于签名的接口，可以自己实现，后边着重说这个实现
			// 摘要算法
			ExternalDigest digest = new BouncyCastleDigest();
			// 签名算法
			ExternalSignature signature = new PrivateKeySignature(pk,
					DigestAlgorithms.SHA1, null);
			// 调用itext签名方法完成pdf签章CryptoStandard.CMS 签名方式，建议采用这种
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
