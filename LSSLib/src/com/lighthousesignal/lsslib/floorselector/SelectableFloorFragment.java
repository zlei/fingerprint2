package com.lighthousesignal.lsslib.floorselector;

import com.lighthousesignal.lsslib.NetworkListener;
import com.lighthousesignal.lsslib.NetworkResult;
import com.lighthousesignal.lsslib.NetworkTask;
import com.lighthousesignal.lsslib.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SelectableFloorFragment extends Fragment implements NetworkListener {
	
	private ImageView iv;
	
    public static SelectableFloorFragment newInstance(Bitmap image, String name, String imagePath, int floorId){
        SelectableFloorFragment f = new SelectableFloorFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putParcelable("image", image);
        args.putString("imagePath", imagePath);
        args.putInt("floorId", floorId);
        f.setArguments(args);

        return f;
    }
	
	public SelectableFloorFragment() { };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (LinearLayout)inflater.inflate(R.layout.selectable_floor_fragment, container, false);
		iv = (ImageView)view.findViewById(R.id.imageView1);
		iv.setImageBitmap((Bitmap)this.getArguments().getParcelable("image"));
		
		if(this.getArguments().getParcelable("image") == null) {
			NetworkTask task = new NetworkTask(this, NetworkListener.GET_IMAGE, this.getArguments().getString("imagePath"), true, false, "", this.getArguments().getInt("floorId"));
			task.execute();
		}
		
		
		return view;
	}
	
	public String getName() {
		return this.getArguments().getString("name");
	}

	@Override
	public void onTaskSuccess(NetworkResult result) {
		if(result.getResultType() == GET_IMAGE) {
    		try {
    			byte[] data = result.getData();
    			result.setData(null);
    			byte[] byteArrayForBitmap = new byte[16*1024]; 
    			BitmapFactory.Options opt = new BitmapFactory.Options(); 
				opt.inTempStorage =  byteArrayForBitmap; 
				Bitmap mapImage = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
				
				iv.setImageBitmap(mapImage);
				
				data = null;
				byteArrayForBitmap = null;					
			} catch (OutOfMemoryError ex) {
				//System.out.println("Out of memory.");
			}
		}
	}

	@Override
	public void onTaskError(NetworkResult result) {
	}
}
