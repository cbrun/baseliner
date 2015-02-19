package fr.obeo.baseliner;

public class TextileFormat implements ReportFormat {

	@Override
	public String packageSection(String key) {
		return "\n\nh3. Package @" + key + "@\n\n";
	}

	@Override
	public String section(String title) {
		return "\n\nh2. " + title + " :\n\n";
	}

	@Override
	public String startList(int deph, String prettyType, String name) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < deph; i++) {
			result.append('*');
		}
		result.append(" In " + prettyType + " @" + name + "@:");
		return result.toString();
	}

	@Override
	public String change(int deph, String name, String prettyType, String prettyChange) {
		StringBuffer result = new StringBuffer();	
		result.append(" the @" + name + "@ " + prettyType + " has been *" + prettyChange + "*");
		return result.toString();
	}

	@Override
	public String endList(int deph) {
		return "";
	}

	@Override
	public String beginListItem(int depth) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			result.append('*');
		}
		return result.toString();
	}

	@Override
	public String endListItem(int depth) {
		return "";
	}
}
