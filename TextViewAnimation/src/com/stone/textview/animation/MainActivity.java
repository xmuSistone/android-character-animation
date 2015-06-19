package com.stone.textview.animation;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private int showIndex = 0;
		private List<String> strList = new ArrayList<String>();

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			final AnimTextView animTv = (AnimTextView) rootView
					.findViewById(R.id.animText);

			String showText1 = "待到秋来九月八，我花开后百花杀。\n冲天香阵透长安，满城尽带黄金甲。";
			String showText2 = "天下事有难易乎？为之，则难者亦易矣；不为，则易者亦难矣。";
			String showText3 = "马之千里者，一食或尽粟一石。食马者不知其能千里而食也。是马也，虽有千里之能，食不饱，力不足，才美不外见，且欲与常马等不可得，安求其能千里也？";

			strList.add(showText3);
			strList.add(showText2);
			strList.add(showText1);

			Button btn = (Button) rootView.findViewById(R.id.fragment_btn);
			btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String showText = strList.get(showIndex % 3);
					animTv.setShowText(showText);
					showIndex++;
				}
			});

			return rootView;
		}
	}

}
