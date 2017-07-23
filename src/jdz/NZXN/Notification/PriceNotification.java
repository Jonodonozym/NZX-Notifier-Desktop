/**
 * PriceNotification.java
 *
 * Created by Jaiden Baker on Jul 13, 2017 11:36:27 AM
 * Copyright � 2017. All rights reserved.
 * 
 * Last modified on Jul 11, 2017 4:52:19 PM
 */

package jdz.NZXN.Notification;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import jdz.NZXN.WebApi.MNZXWebApi;
import jdz.NZXN.utils.JHyperlink;

/**
 * Notification class that displays a price on the screen
 *
 * @author Jaiden Baker
 */
@SuppressWarnings("serial")
public class PriceNotification extends Notification{
	private static Font priceFont = new Font("Calibri", Font.PLAIN, 40);
	private static Font secFont = new Font("Calibri", Font.PLAIN, 20);
	private static int width = 480;
	private String security;
	private double price, oldPrice;
	
	public PriceNotification(String security, double price, double oldPrice) {
		super();
		
		this.security = security;
		this.price = price;
		this.oldPrice = oldPrice;
		setMinimumSize(new Dimension(width,144));
		
	    super.displayContents();
	}
	
	@Override
	protected JPanel getNotificationPanel() {
		final JPanel contentPanel = new JPanel();
		contentPanel.setName(security+" Price Alert");
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
		
		String arrow = oldPrice < price ?
				"<font color=#49A942>&#x25B2;</font>":
				"<font color=#C12828>&#x25BC;</font>";
		arrow = oldPrice == price ? "" : arrow;
		
		JLabel priceLabel = new JHyperlink("<html>"+arrow+"&nbsp;&nbsp;"+price+"�&nbsp;&nbsp;"+arrow+"</html>" , MNZXWebApi.securityURL+security);
		JLabel secLabel = new JHyperlink(security,MNZXWebApi.securityURL+security);
		
		priceLabel.setFont(priceFont);
		secLabel.setFont(secFont);

		priceLabel.setHorizontalAlignment(SwingConstants.CENTER);
		secLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		contentPanel.add(priceLabel,BorderLayout.CENTER);
		contentPanel.add(secLabel,BorderLayout.PAGE_END);
		return contentPanel;
	}
}