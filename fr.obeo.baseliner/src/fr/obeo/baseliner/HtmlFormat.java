package fr.obeo.baseliner;

import com.google.common.base.Function;
import com.google.common.html.HtmlEscapers;

public class HtmlFormat implements ReportFormat {

	
	private Function<String,String> escaper = HtmlEscapers.htmlEscaper().asFunction();
	
	@Override
	public String packageSection(String key) {
		return "\n<h3>Changes in  <code>" + key + "</code></h3>\n";
	}

	@Override
	public String section(String title) {
		return "\n<h2>" + title + "</h2>\n";
	}

	@Override
	public String startList(int deph, String prettyType, String name) {
		StringBuffer result = new StringBuffer();		
		result.append("In " + escape(prettyType) + " <tt>" + escape(name) + "</tt>:\n");
		indent(deph, result);
		result.append("<ul>");
		return result.toString();
	}

	private String escape(String str) {
		return escaper.apply(str);
	}

	@Override
	public String change(int deph, String name, String prettyType, ChangeQualification prettyChange) {
		StringBuffer b = new StringBuffer();
		b.append("<span class=\"" + getCssClass(prettyChange) + "\">");
		b.append(escape(prettyChange.getLabel()));
		b.append("</span>");
		b.append(" the <code>" + escape(name) + "</code> " + escape(prettyType));
		return b.toString();
	}

	private String getCssClass(ChangeQualification prettyChange) {
		switch (prettyChange) {
		case ADDED:
			return "label label-success";
		case REMOVED:
			return "label label-danger";
		case MAJOR:
			return "label label-danger";
		case MICRO:
			return "label label-info";
		case MINOR:
			return "label label-info";
		case UNCHANGED:
			return "label label-info";
		default:
			break;
		}
		return "";
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

	@Override
	public String beginDocument() {
		return "<html><head><style>" + getStylesheetContent() +"</style></head><body>";
	}

	private String getStylesheetContent() {
		return "body,p{line-height:18px;font-family:\"Helvetica Neue\",Helvetica,Arial,sans-serif;font-size:13px}h1,h2{line-height:36px}body,h4,p{line-height:18px}.label,h1,h2,h3,h4,strong{font-weight:700}html{font-size:100%;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%}a:focus{outline:dotted thin;outline:-webkit-focus-ring-color auto 5px;outline-offset:-2px}a:active,a:hover{outline:0}img{max-width:100%;height:auto;border:0;-ms-interpolation-mode:bicubic}body{background-color:#fff}a{color:#08c;text-decoration:none}p{margin:0 0 9px}h1{font-size:30px}h2{font-size:24px}h3{line-height:27px;font-size:18px}h4{font-size:14px}ul{padding:0;margin:0 0 9px 25px;list-style:disc}ul ul{margin-bottom:0}em{font-style:italic}code{font-family:Menlo,Monaco,\"Courier New\",monospace;font-size:12px;border-radius:3px;padding:3px 4px}.label,code{-webkit-border-radius:3px;-moz-border-radius:3px}:-moz-placeholder{color:#999}.label{padding:1px 3px 2px;font-size:9.75px;color:#fff;text-transform:uppercase;background-color:#999;border-radius:3px}a:hover,body{color:#383838}.label-warning{background-color:#f89406}.label-success{background-color:#468847}.label-info{background-color:#3a87ad}body{width:1200px;margin:0 auto;padding-bottom:30px}@media (max-width :1224px){body{background-color:#fff;width:690px;margin:0 auto;padding-bottom:30px;color:#383838}img{max-width:70%}}a:hover{text-decoration:underline}h1,h2,h3,h4{margin:0;text-rendering:optimizelegibility;padding:12px 0;color:#000}ul ul{padding-left:15px}li{line-height:22px}img{box-shadow:0 2px 6px #999;margin:1.5em auto 3em;display:block}code{color:#25587E;background-color:#fafafc!important;border:none!important}.label-danger{background-color:#d9534f}";
	}

	@Override
	public String endDocument() {
		return "</body>";
	}
}
