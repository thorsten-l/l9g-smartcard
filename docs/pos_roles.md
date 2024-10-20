# pos_roles

| Rollenname | URI | Berechtigung |
|------------|-----|--------------|
| LOCKED | | Gesperrt im gesamten System |
| CASHIER (tenant,point of sales) | / | - Kann sich nur am Web-Browser des Kassensystems anmelden <br/> - Kann Bar und SumUp Zahlungen entgegen nehmen <br/> - Kann die Transaktionen am Kassensystem einsehen <br/> - Kann das Kassenbuch einsehen. | 
| ACCOUNTANT (tenant) | /accountant | - Kann sich an einem beliebigen Web-Browser anmelden <br/> - Kann alle Transaktionen und Kassenbestände des zugeordneten Mandanten einsehen |
| ACCOUNTANT (system) | /accountant | *Innenrevision* <br/>- Kann sich an einem beliebigen Web-Browser anmelden <br/> - Kann alle Transaktionen und Kassenbestände aller Mandanten einsehen |
| OWNER (tenant) | /owner | - Kann sich an einem beliebigen Web-Browser anmelden <br/> - Kann alle Transaktionen und Kassenbestände des zugeordneten Mandanten einsehen <br/> - Kann Kategorien, Produkte und Variationen seines Mandanten einstellen </br> - Kann die Rollen [LOCKED,CASHIER,ACCOUNTANT,OWNER] in seinem Mandanten zuordnen. <br/> - Kann Barbestand des Kassenbuches korrigieren. |
| ADMINISTRATOR (system) | /admin | - Kann Mandanten (tenants) anlegen <br/> - Kann alle Rollen zuweisen <br/> - Hat alle Möglichkeiten des OWNERs innerhalb des gewählen Mandanten.  |

 