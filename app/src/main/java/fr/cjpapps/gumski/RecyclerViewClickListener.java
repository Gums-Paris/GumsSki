package fr.cjpapps.gumski;

import android.view.View;

public interface RecyclerViewClickListener {

/*  idée de Pierce Zaifman (entre autres parce que beaucoup de gens se lamentent de ce que RecyclerView n'ait pas le
*   ItemClickListener comme cela existe dans ListView)
*   https://android.jlelse.eu/click-listener-for-recyclerview-adapter-2d17a6f6f6c9
*    Il y a en fait plusieurs manières de faire un itemClickListener dans RecyclerView. Voir Google et bonne chance ! */

/*  Cette interface permet d'avoir un clickListener transportant la position de l'item cliqué dans la liste de la RecyclerView
    ce qui permet de sortir le listener de l'adapteur. On peut alors avoir un adapteur générique et faire tout le travail
    du click dans l'activité ou le fragment qui gère la RecyclerView*/

    void onClick(View view, int position);

}

/*  Exemple d'utilisation dans un fragment où on crée le listener
 *
 *                     RecyclerViewClickListener listener = new RecyclerViewClickListener() {
 *                        @Override
 *                        public void onClick(View view, int position) {
 *                           String element = lesData.get(position);
 *                           model.trouverNomLieu(element);
 *                           model.extractHoriz(element);
 *                           dismiss();
 *                        }
 *                    };
 */

