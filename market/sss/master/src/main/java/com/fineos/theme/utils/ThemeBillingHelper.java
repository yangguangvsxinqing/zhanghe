package com.fineos.theme.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.fineos.billing.BillingWrap;
import com.fineos.billing.util.IabHelper;
import com.fineos.billing.util.IabResult;
import com.fineos.billing.util.Inventory;
import com.fineos.billing.util.Purchase;
import com.fineos.billing.util.IabHelper.OnConsumeFinishedListener;

public class ThemeBillingHelper {

	private static final String TAG = "GoogleBilling.ThemeBillingHelper";

	private BillingWrap mBillingHelper = null;

	private ArrayList<String> mSkuList = new ArrayList<String>();

	private String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArvv19JpIfCUs6Q+LB+R0a74inI6h8/t4RDB40MuSHfbc7n3OjMNKsX7KLh1rTYnbLqRXNoUPsa1HxpNOOBVd7kHzO2wiy3v3rFGhak6DJjoOnGGAGj5GMmDFR+0o8pOYAStdgExfFrz2qzbyVEF/nX2S4SLaS6a/Di5wm2daAyKP1rbrvju0MWzd0rDPLFHyOddRQCfQTOvps9Hhp6Fhne3SYs3945QJE2OFKBmSdI/DuPKcxZcs0tFyEKUIckrKqpf/CbxsNotH1ZzWSOwfe16Vl5hKFHKh1+DG8U3Gum+EHwGdQ4OFYdva5Z7DX9JJnJ9yNHYzc3MTimgp5l7xUwIDAQAB" ;
	private boolean mbNeedRestore = false; // buyed but not give the user

	private HashMap<String, Boolean> mNeedRestores = new HashMap<String, Boolean>();
	
	public ThemeBillingHelper(Context context,ArrayList<String> skuList) {
		
		mBillingHelper = new BillingWrap(context, base64EncodedPublicKey);
		mSkuList = skuList ;
	}
	
	// /xuqian add for billing begin
	
	public boolean isNeedRestore(String sku){
		
		 if(sku==null||"".equals(sku)||mNeedRestores==null||mNeedRestores.size()<1){
			 return false ;
		 }
		 
		 return mNeedRestores.get(sku)==null?false:mNeedRestores.get(sku) ;
	}
	
	public void startSetup(IabHelper.OnIabSetupFinishedListener listener) {

		 if(mBillingHelper!=null){
            mBillingHelper.startSetup(listener);
         }
                 
		
	}

	public void queryInventoryAsync() {
          
       if(mBillingHelper==null){
          return ;
       }

		mBillingHelper
				.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {

					@Override
					public void onQueryInventoryFinished(IabResult result,
							Inventory inventory) {
						// TODO Auto-generated method stub

						if(mSkuList!=null){
							
							for(String thisSku : mSkuList){
								
								for (int i = 0; i < mSkuList.size(); i++) {
									
									Purchase packagePurchase = null ; 
									if(mSkuList.get(i)!=null&&inventory!=null){
										packagePurchase =inventory
										.getPurchase(mSkuList.get(i));
									}
									mbNeedRestore = (packagePurchase != null);
									if(mSkuList.get(i)!=null){
										mNeedRestores.put(mSkuList.get(i), mbNeedRestore);
									}
									ThemeLog.d(TAG, "mbNeedRestore " + mbNeedRestore+",sku:"+mSkuList.get(i));
								}
								
							}
						}
						
					}
				});
	}

	public  HashMap<String, String> getPrice(ArrayList<String> skuList) {

		int n = skuList.size() / 20;
		int mod = skuList.size() % 20;
		ArrayList<ArrayList<String>> packs = new ArrayList<ArrayList<String>>();
		ArrayList<String> tempList;
		
		HashMap<String, String> priceMap = new HashMap<String, String>() ;
		
		for (int i = 0 ; i < n ; i++) {
		         
			tempList = new ArrayList<String>();
		         
			for (String s : skuList.subList(i*20, i*20+20)) {
		         
				tempList.add(s);
		         
			}
		         
			packs.add(tempList);
		         
		}
		         
		if (mod != 0) {
		         
			tempList = new ArrayList<String>();
		         
			for (String s : skuList.subList(n*20, n*20+mod)) {
		         
				tempList.add(s);
		         
			}
		         
			packs.add(tempList);
		         
		}
		for (ArrayList<String> skuPartList : packs) {
			
			HashMap<String, String> priceMapTemp = new HashMap<String, String>() ;
			 
             if(mBillingHelper!=null){
                priceMapTemp = mBillingHelper.getPrice(skuPartList);
             }
			
			Iterator iter = priceMapTemp.entrySet().iterator();
			while (iter.hasNext()) {
			   HashMap.Entry entry = (HashMap.Entry) iter.next();
			   Object key = entry.getKey();
			   Object value = entry.getValue();
			   
			   if(!priceMap.containsKey(key)){
				   priceMap.put(key.toString(), value.toString());
			   }
		   }
		}
		
		   return priceMap ;
	}

	public  void doPurchase(final String sku ,IabHelper.OnIabPurchaseFinishedListener listener) {

		//consumeAsync();  
        if(mBillingHelper!=null){
            mBillingHelper.doPurchase(sku,listener);
        }
		
				  
	}
	
	
	public  void consumeAsync(Purchase purchase){
                
       if(mBillingHelper==null){
            return ;
       }
		
		mBillingHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
			
			@Override
			public void onConsumeFinished(Purchase purchase, IabResult result) {
				// TODO Auto-generated method stub
				ThemeLog.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
				if (result.isSuccess()) {
				   ThemeLog.d(TAG, "Consumption successful.");
				}
			}
		});
		
	}
   public  void consumeAsync(){

           if(mBillingHelper==null){
                return ;
           }
		
		List<Purchase> purchases = getOwnedPurchases(); 
	   mBillingHelper.consumeAsync(purchases, new IabHelper.OnConsumeMultiFinishedListener() {
			
			@Override
			public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
				// TODO Auto-generated method stub
				ThemeLog.v(TAG,"purchases:"+purchases+",results:"+results);
			}
		}); 
		
		
		
	}
	
   public boolean isBuyed(String sku){
	   
	   ArrayList<String> ownedSkus = getOwnedSkus() ;
	   if(sku==null||"".equals(sku)||ownedSkus==null) return false ;
	   boolean isBuyed = ownedSkus.contains(sku);
	   ThemeLog.v(TAG, "sku :"+sku+",isBuyed:"+isBuyed+",ownedSkus:"+ownedSkus);
	   return isBuyed;
   }
   
	public ArrayList<String> getOwnedSkus(){
		
		return mBillingHelper!=null?mBillingHelper.getOwnedSkus():null;
		
	}
	
	 public List<Purchase> getOwnedPurchases(){
		
		return mBillingHelper!=null?mBillingHelper.getOwnedPurchases():null;
		
	}
	
	
	
	
	
	
	

	public  boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		ThemeLog.v(TAG, "onActivityResult");
		return mBillingHelper.onActivityResult(requestCode, resultCode, data);
		

	}

	public  void onDestroy() {
		// TODO Auto-generated method stub
		ThemeLog.v(TAG, "onDestroy()");
		mBillingHelper.onDestroy();
	}

	// /xuqian add for billing end

}
