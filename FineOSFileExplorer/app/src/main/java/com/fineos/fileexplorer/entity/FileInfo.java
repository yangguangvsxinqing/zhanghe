package com.fineos.fileexplorer.entity;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.fineos.fileexplorer.R;

import java.io.File;

public class FileInfo {

	private final static String TAG = FileInfo.class.getSimpleName();
	private static final String VOLUME_NAME = "external";
	private FileCategory fileCategory;
	private String fileName;
	private String filePath;
	private boolean canRead;
	private boolean canWrite;
	private boolean isDirectory;
	private boolean isHidden;
	private boolean isFileSelected;
	private Long lastModified;
	private int subFileCount = -1;
	private Long length;
	private Bitmap thumbnail;
    private String mMimeType;


	private long dbId; // id in the database, if is from database.

	public FileInfo(File file, Boolean showHiddenFiles) {
		this(file, FileCategory.getFileCategory(file), showHiddenFiles);
	}

	//return -1 if the file is not a directory.
	private int getSubFilesCount(File file, boolean showHiddenFiles) {
		int fileCount = -1;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if(files != null){
				fileCount = 0;
				for(File subFile : files){
					if(!subFile.getName().startsWith(".") || showHiddenFiles){
						fileCount++;
					}
				}
			}else{
				return fileCount;
			}
		}
		return fileCount;
	}

	public FileInfo(File file, FileCategory fileCategory, Boolean showHiddenFiles) {
		this.fileCategory = fileCategory;
		this.fileName = file.getName();
		this.filePath = file.getPath();
		this.canRead = file.canRead();
		this.canWrite = file.canWrite();
		this.isDirectory = file.isDirectory();
		this.isHidden = file.isHidden();
		this.isFileSelected = false;
		this.lastModified = file.lastModified();
		this.subFileCount = getSubFilesCount(file, showHiddenFiles);
		this.length = file.length();
	}
	

	public boolean isFileSelected() {
		return isFileSelected;
	}

	public void setFileSelected(boolean isFileSelected) {
		this.isFileSelected = isFileSelected;
	}
	
	public FileCategory getFileCategory() {
		return fileCategory;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public int getSubFilesCount() {
		return subFileCount;
	}
	
	public Long getLength() {
		return length;
	}
	
	public Bitmap getThumbnail(Context context) {
		if(thumbnail == null && this.getFileCategory() == FileCategory.PIC){
			new ThumbnailLoadTask().execute(context);
		}
		return thumbnail;
	}
	
	public Bitmap getThumbnail(){
		return thumbnail;
	}

	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
	}

    private class ThumbnailLoadTask extends AsyncTask<Context, Void, Bitmap>{
		@Override
		protected Bitmap doInBackground(Context... params) {
			Context context = params[0];
			Bitmap thumbnail = null;
			try {
				thumbnail = getThumbnail(context.getContentResolver(), filePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return thumbnail;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if(result != null){
				thumbnail = result;
			}
			super.onPostExecute(result);
		}
		
	}
	
	public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {
	    Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
	    if (ca != null && ca.moveToFirst()) {
	        int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
	        ca.close();
	        return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null );
	    }
	    ca.close();
	    return null;
	}

	public long getDbId() {
		return dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	// Definition of enum type FileCategory:
	public enum FileCategory {
		PIC(".jpg.jpeg.gif.png.bmp.wbmp"), 
		MUSIC(".mp3.wma.wav.mid.ogg.amr.aac.ape.awb.mp2.3gpp.m4a.midi.mid.kar.xmf.flac.imy"),
		VIDEO(".wmv.mp4.mpeg.m4v.3gp.3g2.3gpp2.asf.avi.flv.mkv.mov.ape"),
		APK(".apk"), 
		DOC(".doc.ppt.docx.pptx.xsl.xslx.pdf.txt.log.xml.ini.lrc"),
		ZIP(".zip.rar.7z"),
		OTHER("");

		private final static String TAG = FileCategory.class.getSimpleName();

		private String fileSuffix;

		FileCategory(String suffix) {
			fileSuffix = suffix;
		}

		@Override
		public String toString() {
			return name();
		}

		public static FileCategory getFileCategory(FileInfo file) {
			if (file.isDirectory()) {
				return null;
			}
			return getFileCategory(file.getFileName());
		}

        public static FileCategory getCategoryByName(String categoryName){
            for(FileCategory category : FileCategory.values()){
                if(category.name().equals(categoryName)){
                    return category;
                }
            }
            return null;
        }
		
		public static FileCategory getFileCategory(File file) {
			if (file.isDirectory()) {
				return null;
			}
			return getFileCategory(file.getName());
		}
		

		public static FileCategory getFileCategory(String filename) {
			String lowercaseFilename = filename.toLowerCase();
			int lastIndexOfDot = lowercaseFilename.lastIndexOf('.');
			// if filename starts with "."(a hidden file in linux) or filename
			// not contains ".", or the filename contians nothing after ".", return OTHER as the file type.
			if (lowercaseFilename.startsWith(".") || lastIndexOfDot == -1 || lastIndexOfDot == lowercaseFilename.length() - 1)
				return FileCategory.OTHER;
			for (FileCategory fileCategory : FileCategory.values()) {
				if (fileCategory.fileSuffix.contains(lowercaseFilename
						.substring(lastIndexOfDot))) {
                    String[] suffixStringArray = fileCategory.fileSuffix.split("\\.");
                    for (String s : suffixStringArray) {
                        String extName = lowercaseFilename
                                .substring(lastIndexOfDot+1);
                        if(s.equals(extName)){
                            return fileCategory;
                        }
                    }
                    //return fileCategory;
				}
			}
			// if not contained in any types, return OTHER as the file type:
			return FileCategory.OTHER;
		}

		public static int getImageResourceByCategory(FileCategory fileCategory,
				String filename) {
			switch (fileCategory) {
			case PIC:
				return R.drawable.ic_picture;
			case MUSIC:
				return R.drawable.ic_music;
			case VIDEO:
				return R.drawable.ic_videos;
			case APK:
				return R.drawable.ic_apks;
			case ZIP:
				return R.drawable.ic_zip;
			case OTHER:
				return R.drawable.ic_other;
			case DOC:
				if (filename.endsWith(".ppt")) {
					return R.drawable.ic_ppt;
				}
				if (filename.endsWith(".pdf")) {
					return R.drawable.ic_pdf;
				}
				if (filename.endsWith(".xls")) {
					return R.drawable.ic_xls;
				}
				return R.drawable.ic_doc;
			default:
				break;
			}
			return 0;
		}

        public static int getCategoryStringResource(FileCategory category){
            switch (category) {
                case PIC:
                    return R.string.picture_text;
                case MUSIC:
                    return R.string.music_text;
                case VIDEO:
                    return R.string.video_text;
                case APK:
                    return R.string.apk_text;
                case ZIP:
                    return R.string.zip_text;
                case OTHER:
                    return R.string.other_text;
                case DOC:
                    return R.string.doc_text;
                default:
                    break;
            }
            return 0;
        }

		public static int getImageResourceByFile(FileInfo file) {
			if (file.isDirectory() || file.getFileCategory() == null) {
				return R.drawable.ic_file;
			}
			return getImageResourceByCategory(file.getFileCategory(), file.getFileName());
		}
		
		public static int getImageResourceByFile(File file) {
			if (file.isDirectory()) {
				return R.drawable.ic_file;
			}
			FileCategory fileCategory = getFileCategory(file);
			if(fileCategory == null){
				return R.drawable.ic_file;
			}
			return getImageResourceByCategory(fileCategory, file.getName());
		}
		
		public static Uri getContentUriByCategory(FileCategory fileCategory) {
			Uri uri;
			switch (fileCategory) {
			case DOC:
			case ZIP:
			case APK:
				uri = Files.getContentUri(VOLUME_NAME);
				break;
			case MUSIC:
				uri = Audio.Media.getContentUri(VOLUME_NAME);
				break;
			case VIDEO:
				uri = Video.Media.getContentUri(VOLUME_NAME);
				break;
			case PIC:
				uri = Images.Media.getContentUri(VOLUME_NAME);
				break;
			default:
				Log.d(TAG, "cannot get uri by given FileCategory : " + fileCategory);
				uri = null;
			}
			return uri;
		}


    }
}
