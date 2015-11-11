using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace SearchClient.Controllers
{
    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            ViewBag.Title = "Home Page";
            this.HttpContext.Response.Headers.Add("Access-Control-Allow-Origin", "http://localhost:8080");
            return View();
        }
    }
}
