package fr.obeo.baseliner;

public class HtmlFormat implements ReportFormat {

	@Override
	public String packageSection(String key) {
		return "\n<h3>Package <b>" + key + "</b></h3>\n";
	}

	@Override
	public String section(String title) {
		return "\n<h2>" + title + "</h2>\n";
	}

	@Override
	public String startList(int deph, String prettyType, String name) {
		StringBuffer result = new StringBuffer();		
		result.append("In " + prettyType + " <tt>" + name + "</tt>:\n");
		indent(deph, result);
		result.append("<ul>");

		return result.toString();
	}

	@Override
	public String change(int deph, String name, String prettyType, String prettyChange) {
		StringBuffer result = new StringBuffer();
		result.append("the <tt>" + name + "</tt> " + prettyType + " has been <b>" + prettyChange + "</b>");
		return result.toString();
	}

	@Override
	public String endList(int deph) {
		StringBuffer result = new StringBuffer();
		result.append("\n");
		indent(deph, result);
		result.append("</ul>");
		return result.toString();
	}

	private void indent(int deph, StringBuffer result) {
		for (int i = 0; i < deph * 2; i++) {
			result.append(' ');
		}
	}

	@Override
	public String beginListItem(int depth) {
		StringBuffer result = new StringBuffer();
		result.append("\n");
		indent(depth + 1, result);
		result.append("<li>");
		return result.toString();
	}

	@Override
	public String endListItem(int depth) {
		StringBuffer result = new StringBuffer();
		result.append("\n");
		indent(depth + 1, result);
		result.append("</li>");
		return result.toString();
	}
}
