package fr.obeo.baseliner;

public interface ReportFormat {

	String beginDocument();
	
	String endDocument();
	
	String packageSection(String key);

	String section(String title);

	String startList(int deph, String prettyType, String name);

	String change(int deph, String name, String prettyType, ChangeQualification prettyChange);

	String endList(int deph);

	String beginListItem(int depth);

	String endListItem(int depth);

	public enum ChangeQualification {

		ADDED("added"), REMOVED("removed"), UNCHANGED("unchanged"), MAJOR("changed in an incompatible way"), MICRO(
				"changed"), MINOR("extended");

		private final String label;

		private ChangeQualification(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

}
