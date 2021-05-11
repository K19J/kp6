package by.bsuir.filinovichsa.adkpproject.servlets;

import by.bsuir.filinovichsa.adkpproject.ad.AbstractAd;
import by.bsuir.filinovichsa.adkpproject.factories.AdFactory;
import by.bsuir.filinovichsa.adkpproject.products.Deal;
import by.bsuir.filinovichsa.adkpproject.products.Product;
import by.bsuir.filinovichsa.adkpproject.services.AdService;
import by.bsuir.filinovichsa.adkpproject.services.DealService;
import by.bsuir.filinovichsa.adkpproject.services.UserService;
import by.bsuir.filinovichsa.adkpproject.users.Advertiser;
import by.bsuir.filinovichsa.adkpproject.users.Distributor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/sendDealToDist")
public class SendDealToDistServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.removeAttribute("distributor");
        req.getRequestDispatcher("/chooseDistributor.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            HttpSession session = req.getSession(false);
            Advertiser advertiser = (Advertiser) session.getAttribute("user");
            Distributor distributor = (Distributor) session.getAttribute("distributor");
            Product product = (Product) session.getAttribute("product");

            Object[] parameters = Servlets.readAdByRequest(req, product.getAdType());
//        switch(product.getAdType()) {
//            case AIRSHIP:
//                int numberOfDays = Integer.parseInt(Servlets.getStringParameter(req, "numberOfDays"));
//                int length = Integer.parseInt(Servlets.getStringParameter(req, "length"));
//                int width = Integer.parseInt(Servlets.getStringParameter(req, "width"));
//                String startTime = Servlets.getStringParameter(req, "startTime");
//                String description = Servlets.getStringParameter(req, "adDescription");
//                parameters = new Object[]{numberOfDays, length, width, startTime, description};
//                break;
//            case BALLOON:
//                int numberOfHours = Integer.parseInt(Servlets.getStringParameter(req, "numberOfHours"));
//                float pricePerHour = Float.parseFloat(Servlets.getStringParameter(req, "pricePerHour"));
//                String newBalloonString = req.getParameter("newBalloon");
//                boolean newBalloon;
//                newBalloon = newBalloonString != null;
//                startTime = Servlets.getStringParameter(req, "startTime");
//                description = Servlets.getStringParameter(req, "adDescription");
//                parameters = new Object[]{numberOfHours, pricePerHour, newBalloon, startTime, description};
//            case EVENT:
//            case OUTDOOR:
//            case INTERNET:
//            case SOUVENIR:
//            case MEDIAN_NET:
//            case OTH_PRINTED:
//            case MEDIA_PRINTED:
//            case BROADCAST:
//        }
//        //TODO:
            AdFactory factory = new AdFactory();
            AbstractAd abstractAd = factory.createAd(product.getAdType(), parameters);
            AdService adService = AdService.getInstance();
            adService.save(abstractAd);
            //advertiser.addProduct(product);
            String comment = Servlets.getStringParameter(req, "comment");
            Deal deal = new Deal(product, abstractAd, distributor, Deal.Status.TO_DISTRIBUTOR, comment);
            deal.getAd().setDeal(deal);
            DealService dealService = DealService.getInstance();
            dealService.save(deal);
            deal = dealService.findById(deal.getId());
            Servlets.refreshAdvertiser(req);

            deal.load();
            req.setAttribute("deal", deal);
            req.getRequestDispatcher("/deal.jsp").forward(req, resp);
        } catch (Exception e) {
            req.getRequestDispatcher("/myProducts.jsp").forward(req, resp);
        }
    }
}
