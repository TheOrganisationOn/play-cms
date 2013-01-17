package views.tags.cms;

import static org.apache.commons.lang.StringUtils.*;
import static org.jsoup.Jsoup.*;
import groovy.lang.Closure;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import models.cms.CMSPage;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Scope;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.JavaExtensions;
import controllers.cms.Admin;
import controllers.cms.Profiler;

@FastTags.Namespace("cms")
public class Tags extends FastTags {

	/**
	 * Displays page from CMS by name given by argument. Displays edit button for this page at bottom.
	 */
	public static void _display(Map<?, ?> args, Closure body, PrintWriter out,
			ExecutableTemplate template, int fromLine) throws Throwable {

		CMSPage page = displayCmsPage(args, body, out, template);
		displayEditButton(out, page);
	}

	/**
	 * Displays page content from CMS by name given by argument without edit button.
	 */
	public static void _displayText(Map<?, ?> args, Closure body, PrintWriter out,
			ExecutableTemplate template, int fromLine) throws Throwable {

		displayCmsPage(args, body, out, template);
	}

	/**
	 * Displays only edit button for given pageName.
	 */
	public static void _edit(Map<?, ?> args, Closure body, PrintWriter out,
			ExecutableTemplate template, int fromLine) throws Throwable {

		String pageName = (String) args.get("arg");
		edit(out, pageName);
	}

	private static void displayEditButton(PrintWriter out, CMSPage page) throws Throwable {

		addReferrerToCurrentPage();
		edit(out, page.name);
	}

	private static void addReferrerToCurrentPage() {
		Scope.Session.current.get().put(Admin.REFERRER_PARAM, Http.Request.current.get().url);
	}

	private static CMSPage displayCmsPage(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template) {
		String pageName = (String) args.get("arg");
		Boolean stripHtml = (Boolean) args.get("stripHtml");
		CMSPage page = CMSPage.findById(pageName);

		String safeBody = body != null ? JavaExtensions.toString(body) : "";

		if (page == null) {
			page = new CMSPage();
			page.name = pageName;
			page.title = "Fragment on " + template.template.name;
			page.body = safeBody;
			page.active = false;
			page.save();
			print(out, safeBody, stripHtml);
		} else if (!page.active) {
			print(out, safeBody, stripHtml);
		} else if (page.body != null) {
			print(out, getBodyWithoutHtmlParagraph(page.body), stripHtml);
		}
		return page;
	}

	private static void print(PrintWriter out, String body, Boolean stripHtml) {
		if (BooleanUtils.isTrue(stripHtml)) {
			body = parse(body).text();
		}
		out.print(body);
	}

	private static String getBodyWithoutHtmlParagraph(String body) {
		if (StringUtils.isBlank(body)) {
			return body;
		} else {
			String trimmed = body.trim();
			return removeEnd(removeStart(trimmed, "<p>"), "</p>");
		}
	}

	private static void edit(PrintWriter out, String name) throws Throwable {

		if (!Profiler.canEdit(name)) {
			return;
		}
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("pageName", name);
		out.print("<a class=\"cms-edit\" href=\"" + Router.reverse("cms.Admin.editPage", args) + "\">");
		out.print("<img alt=\"Edit\" src=\"/public/images/edit.gif\">");
		out.print("</a>");
	}
}
