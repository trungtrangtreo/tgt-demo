package ca.TransCanadaTrail.TheGreatTrail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.TransCanadaTrail.TheGreatTrail.R;

import static ca.TransCanadaTrail.TheGreatTrail.ActivityTracker.ActivityTrackerFragment.trackerfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MapView.MapFragment.mapfragStack;
import static ca.TransCanadaTrail.TheGreatTrail.MeasureTool.MeasureFragment.measurefragStack;

/**
 * Created by hardikfumakiya on 2017-01-09.
 */

public class MoreListFragment extends Fragment {
    ListView more_list;
    String itemType;
    String[] google_service;
    MainActivity activity;
    List<String> searchList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity=(MainActivity)context;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.clear();
//    }
//
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.more_list,container,false);
        more_list=(ListView)v.findViewById(R.id.more_list);

        activity.getSupportActionBar().setTitle(Html.fromHtml("<small>"+getArguments().getString("title")+"</small>"));
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        //((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        //getActivity().setTitle(getArguments().getString("title"));
        //setHasOptionsMenu(true);

//        v.setFocusableInTouchMode(true);
//        v.requestFocus();
//        v.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event)   {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//
//                    //SearchListFragment searchFragment=SearchListFragment.getInstance();
////        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
//
//                    FragmentTransaction transaction=getFragmentManager().beginTransaction();
//                    transaction.replace(R.id.output,new SearchListFragment());
//
//                    transaction.commit();
//
//                    return true;
//                }
//                return false;
//            }
//        });
        ArrayAdapter adapter = null;

        itemType=getArguments().getString("itemType");
        try{
            switch (itemType) {
                case "RecentSearch":
                    SharedPreferences preferences = getContext().getSharedPreferences("SearchPreferences", Context.MODE_PRIVATE);
                    Set<String> searchText = preferences.getStringSet("search", new HashSet<String>());
                    searchList = new ArrayList<>(searchText);
                    Collections.sort(searchList, new Comparator<String>()
                    {
                        @Override
                        public int compare(String text1, String text2)
                        {
                            return text1.compareToIgnoreCase(text2);
                        }
                    });
                    //recent_search=searchText.toArray(new String[searchText.size()]);
                    adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,searchList);
                    break;
//                case "TrailService":
//                    trail_service=getResources().getStringArray( R.array.trailServiceArray);
//                    adapter = ArrayAdapter.createFromResource(getContext(), R.array.trailServiceArray, android.R.layout.simple_list_item_1);
//                    break;
                case "GooglePlacesService":
                    google_service=getResources().getStringArray( R.array.googlePlacesServiceArray);
                    List<String> gServicesList = Arrays.asList(google_service);
                    Collections.sort(gServicesList, new Comparator<String>()
                    {
                        @Override
                        public int compare(String text1, String text2)
                        {
                            return text1.compareToIgnoreCase(text2);
                        }
                    });
                    adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,gServicesList);

                    ImageView empty = new ImageView(getContext());
                    AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    empty.setLayoutParams(lp);
                    empty.setPadding(20,30,20,30);
                    empty.setImageResource(R.drawable.powered_by_google_light);

                    more_list.addFooterView(empty);
                    break;
                default:
                    break;
            }

            if(adapter!=null)
                more_list.setAdapter(adapter);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        more_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (itemType)
                {
                    case "RecentSearch":
                        popLastSearchFragmentFromStack();

                        String item=searchList.get(position);
                        SearchListFragment searchFrag= new SearchListFragment();
                        searchFrag.refreshData(activity,item);
                        replaceFragment(SearchListFragment.resourceID,searchFrag);

                        break;
                    case "GooglePlacesService":
                        pushMoreFragmentToStack();

                        GooglePlaceCategoryFragment gfragment= GooglePlaceCategoryFragment.getInstance();
                        gfragment.categoryType=google_service[position];

                        replaceFragment(SearchListFragment.resourceID,gfragment);
                        break;
                    default:
                        break;
                }
            }
        });

        return v;
    }
    private void pushMoreFragmentToStack() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fmore = null;
        String fragmentTag = "MoreListFragment";

        switch (MainActivity.currentTab) {
            case "MapFragment":
                fragmentTag="Map"+fragmentTag;
                fmore = fragmentManager.findFragmentById(R.id.searchLayout);

                break;
            case "MeasureFragment":
                fragmentTag="Measure"+fragmentTag;
                fmore = fragmentManager.findFragmentById(R.id.measureSearchLayout);

                break;
            case "ActivityTrackerFragment":
                fragmentTag="Tracker"+fragmentTag;
                fmore = fragmentManager.findFragmentById(R.id.trackerSearchLayout);
                break;
        }

        if (fmore instanceof MoreListFragment) {
            SearchListFragment.fragStack.put(fragmentTag, fmore);
            SearchListFragment.fragTagStack.push(fragmentTag);
        }
    }

    private void replaceFragment(int resourceID, Fragment replaceFragment) {
        FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();

        mFragmentTransaction
                .replace(resourceID, replaceFragment)
                .commit();
    }

    private void popLastSearchFragmentFromStack() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fsearch = null;
        String searchtag = "SearchFragment";

        switch (MainActivity.currentTab) {
            case "MapFragment":
                //fsearch = fragmentManager.findFragmentById(R.id.searchLayout);
                searchtag="Map" + searchtag;
                break;
            case "MeasureFragment":
                //fsearch = fragmentManager.findFragmentById(R.id.measureSearchLayout);
                searchtag="Measure" + searchtag;
                break;
            case "ActivityTrackerFragment":
                //fsearch = fragmentManager.findFragmentById(R.id.trackerSearchLayout);
                searchtag="Tracker" + searchtag;
                break;
        }

        fsearch= SearchListFragment.fragStack.get(searchtag);

        if (fsearch instanceof SearchListFragment) {
            SearchListFragment.fragStack.remove(searchtag);
            SearchListFragment.fragTagStack.remove(searchtag);

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if(MainActivity.currentTab.equals("MapFragment") && mapfragStack.containsKey("MapSearchFragment")){
            menu.clear();
        }
        else if(MainActivity.currentTab.equals("MeasureFragment") && measurefragStack.containsKey("MeasureSearchFragment")){
            menu.clear();
        }
        else if(MainActivity.currentTab.equals("ActivityTrackerFragment") && trackerfragStack.containsKey("TrackerSearchFragment")){
            menu.clear();
        }

    }
}
