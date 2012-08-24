package controllers.cms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import models.cms.CMSImage;
import models.cms.CMSPage;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.db.jpa.Blob;
import play.libs.MimeTypes;
import play.mvc.Controller;

public class Admin extends Controller {

	public static final String REFERRER_PARAM = "referrer";

	public static void index() {
		if (!Profiler.canEnter())
			forbidden();
		List<CMSPage> pages = CMSPage.all().fetch();
		render(pages);
	}

	public static void editPage(String tmpl, String pageName) {
		if (!Profiler.canEdit(pageName))
			forbidden();
		CMSPage page = CMSPage.findById(pageName);
		if (page == null) {
			page = new CMSPage();
			page.name = pageName;
			page.active = true;
		}
		renderTemplate("@edit", page, tmpl);
	}

	public static void addPage() {
		if (!Profiler.canEnter())
			forbidden();
		CMSPage page = new CMSPage();
		page.active = true;
		renderTemplate("@edit", page);
	}

	public static void savePage(@Valid CMSPage page, boolean active) throws Throwable {
		if (!Profiler.canEdit(page.name))
			forbidden();

		if (request.params.get("cancelEdit") != null) {
			if (session.get(REFERRER_PARAM) != null) {
				redirect(session.get(REFERRER_PARAM));
			} else {
				index();
			}
		}
		page.active = active;
		if (request.params.get("delete") != null) {
			page.delete();
			index();
		}
		page.save();
		if (request.params.get("savePage") != null) {
			if (session.get(REFERRER_PARAM) != null) {
				redirect(session.get(REFERRER_PARAM));
			} else {
				Frontend.show(null, page.name);
			}
		}
		index();
	}

	public static void upload(File data, String title) {
		if (!Profiler.canEnter())
			forbidden();
		CMSImage image = CMSImage.findById(data.getName());
		if (image == null) {
			image = new CMSImage();
			image.name = data.getName();
		}
		if (StringUtils.isEmpty(title))
			image.title = data.getName();
		else
			image.title = title;
		String mimeType = MimeTypes.getContentType(data.getName());
		image.data = new Blob();
		try {
			image.data.set(new FileInputStream(data), mimeType);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		image.save();
		redirect("/public/tiny_mce/plugins/advimage/image.htm?" + image.name);
	}

	public static void imagelist() {
		if (!Profiler.canEnter())
			forbidden();
		List<CMSImage> images = CMSImage.findAll();
		render(images);
	}

}
