package fr.obeo.baseliner;

public interface ReportFormat {

	String packageSection(String key);

	String section(String title);

	String startList(int deph, String prettyType, String name);

	String change(int deph, String name, String prettyType, String prettyChange);

	String endList(int deph);

	String beginListItem(int depth);

	String endListItem(int depth);

}
