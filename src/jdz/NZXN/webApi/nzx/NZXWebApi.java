
package jdz.NZXN.webApi.nzx;

import java.time.LocalDateTime;
import java.util.List;

import jdz.NZXN.config.Config;
import jdz.NZXN.dataStructs.Announcement;

public interface NZXWebApi {
	public static final NZXWebApi instance = new NZXWebApiImp();
	
	public LocalDateTime getDateTime();
	public List<Announcement> getMarketAnnouncements(Config config);
	public float getSecurityValue(String securityCode);
	public void downloadAttatchments(List<Announcement> announcement);
	public boolean canConnect();
}