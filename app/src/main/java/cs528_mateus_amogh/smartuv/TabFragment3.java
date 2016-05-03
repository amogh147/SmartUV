package cs528_mateus_amogh.smartuv;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;


/**
 */
public class TabFragment3 extends Fragment {

    public static final String TAG = "TabFragment3";

    private final Map<Integer, Intent> urlMap= new HashMap<Integer, Intent>();

    private View.OnClickListener mClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            startActivity(urlMap.get(v.getId()));
        }
    };


    public TabFragment3() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urlMap.put(R.id.whoButtonView,new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.whoUrl))));
        urlMap.put(R.id.epaButtonView, new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.epaUrl))));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_fragment3, container, false);

        view.findViewById(R.id.whoButtonView).setOnClickListener(mClickListener);
        view.findViewById(R.id.epaButtonView).setOnClickListener(mClickListener);

        return view;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }
}
