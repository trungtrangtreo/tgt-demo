package ca.TransCanadaTrail.TheGreatTrail.item;

public class SectionItem implements Item {

	private final String title;
	public final int month;
	
	public SectionItem(String title, int month) {
		this.title = title;
        this.month = month;
	}
	
	public String getTitle(){
		return title;
	}
	
	@Override
	public boolean isSection() {
		return true;
	}


}
