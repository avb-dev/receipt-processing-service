package application.entity;

import lombok.Getter;

@Getter
public class HtmlBlank {

    private final String html;

    public HtmlBlank (String printUrl) {

        this.html = "<!DOCTYPE html>\n" +
                "<html lang=\"ru\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Электронный чек</title>\n" +
                "</head>\n" +
                "\n" +
                "<body style=\"margin:0; padding:0; background-color:#323437; font-family: Arial, Helvetica, sans-serif;\">\n" +
                "\n" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#323437; padding:24px 0;\">\n" +
                "  <tr>\n" +
                "    <td align=\"center\">\n" +
                "\n" +
                "      <!-- Контейнер -->\n" +
                "      <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\"\n" +
                "             style=\"background-color:#ffffff; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.06);\">\n" +
                "\n" +
                "        <!-- Header -->\n" +
                "        <tr>\n" +
                "          <td style=\"padding:24px 32px 16px 32px;\">\n" +
                "            <h1 style=\"margin:0; font-size:22px; color:#111;\">\n" +
                "              Ваш электронный чек\n" +
                "            </h1>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "\n" +
                "        <!-- Text -->\n" +
                "        <tr>\n" +
                "          <td style=\"padding:0 32px 24px 32px; color:#555; font-size:14px; line-height:1.5;\">\n" +
                "            Спасибо за покупку!  \n" +
                "            Ниже вы найдёте электронный чек по вашему заказу.\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "\n" +
                "        <!-- Receipt image -->\n" +
                "        <tr>\n" +
                "          <td align=\"center\" style=\"padding:0 32px 24px 32px;\">\n" +
                "            <img\n" +
                "              src=\"" + printUrl + "\"\n" +
                "              alt=\"Электронный чек\"\n" +
                "              style=\"\n" +
                "                width:100%%;\n" +
                "                max-width:420px;\n" +
                "                border-radius:8px;\n" +
                "                border:1px solid #e5e7eb;\n" +
                "                display:block;\n" +
                "              \"\n" +
                "            />\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "\n" +
                "        <!-- Button -->\n" +
                "        <tr>\n" +
                "          <td align=\"center\" style=\"padding-bottom:24px;\">\n" +
                "            <a href=\"" + printUrl + "\"\n" +
                "               style=\"\n" +
                "                 background-color:#eb2585;\n" +
                "                 color:#ffffff;\n" +
                "                 text-decoration:none;\n" +
                "                 padding:12px 24px;\n" +
                "                 border-radius:8px;\n" +
                "                 font-size:14px;\n" +
                "                 display:inline-block;\n" +
                "               \">\n" +
                "              Открыть чек\n" +
                "            </a>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "\n" +
                "        <!-- Footer -->\n" +
                "        <tr>\n" +
                "          <td style=\"padding:16px 32px; border-top:1px solid #eee; font-size:12px; color:#888;\">\n" +
                "            Если изображение не отображается, вы можете открыть чек по ссылке:\n" +
                "            <br/>\n" +
                "            <a href=\"%s\" style=\"color:#2563eb;\">" + printUrl +  "</a>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "\n" +
                "        <tr>\n" +
                "          <td align=\"center\" style=\"padding:16px; font-size:12px; color:#aaa;\">\n" +
                "            © 2025 cosmo-service.org\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "\n" +
                "      </table>\n" +
                "\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }
}
