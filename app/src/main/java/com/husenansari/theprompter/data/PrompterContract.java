package com.husenansari.theprompter.data;


import android.content.ContentResolver;
import android.content.Context;

import java.util.ArrayList;


public class PrompterContract {

    public static final String AUTHORITY = "com.husenansari.theprompter";

    public static final class ScriptEntry {

        public static final String TABLE = "scripts";

        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String TIMESTAMP = "timestamp";

        public static final String TABLE_CREATE_QUERY =
                "create table "+ TABLE + " (" +
                        _ID + " integer primary key autoincrement, "+
                        TITLE + " text not null, "+
                        CONTENT + " text not null, "+
                        TIMESTAMP + " integer not null" +
                        ");";
    }

    public static void addDummyContent(Context context)
    {
        ArrayList<Script> scripts = dummyContent();
        ContentResolver contentResolver = context.getContentResolver();

        for (Script script : scripts) {
            contentResolver.insert(ScriptsProvider.SCRIPTS_BASE_URI, script.toContentValues());
        }
    }

    private static ArrayList<Script> dummyContent()
    {
        ArrayList<Script> scripts = new ArrayList<>();
        Script script = new Script();
        script.setTitle("Cockroach Theory : Sundar Pichai");
        script.setContent("The cockroach theory for self development At a restaurant, a cockroach suddenly flew from somewhere and sat on a lady.\n" +
                "\n" +
                "She started screaming out of fear.With a panic stricken face and trembling voice, she started jumping, with both her hands desperately trying to get rid of the cockroach.\n" +
                "\n" +
                "Her reaction was contagious, as everyone in her group also got panicky. The lady finally managed to push the cockroach away but ...it landed on another lady in the group. Now, it was the turn of the other lady in the group to continue the drama.\n" +
                "\n" +
                "Now, it was the turn of the other lady in the group to continue the drama. The waiter rushed forward to their rescue. In the relay of throwing, the cockroach next fell upon the waiter.\n" +
                "\n" +
                "The waiter stood firm, composed himself and observed the behavior of the cockroach on his shirt. When he was confident enough, he grabbed it with his fingers and threw it out of the restaurant. Sipping my coffee and watching the amusement, the antenna of my mind picked up a few thoughts and started wondering, was the cockroach responsible for their histrionic behavior?\n" +
                "\n" +
                "If so, then why was the waiter not disturbed? He handled it near to perfection, without any chaos. It is not the cockroach, but the inability of those people to handle the disturbance caused by the cockroach, that disturbed the ladies.\n" +
                "\n" +
                "I realized that, it is not the shouting of my father or my boss or my wife that disturbs me, but it's my inability to handle the disturbances caused by their shouting that disturbs me. It's not the traffic jams on the road that disturbs me, but my inability to handle the disturbance caused by the traffic jam that disturbs me.\n" +
                "\n" +
                "More than the problem, it's my reaction to the problem that creates chaos in my life.\n" +
                "\n" +
                "Lessons learnt from the story: I understood, I should not react in life. I should always respond. The women reacted, whereas the waiter responded. Reactions are always instinctive whereas responses are always well thought of. A beautiful way to understand............LIFE. Person who is HAPPY is not because Everything is RIGHT in his Life..  He is HAPPY because his Attitude towards Everything in his Life is Right..!!\n" +
                "\n");
        scripts.add(script);

        script = new Script();
        script.setTitle("On The Eve Of Historic Dandi March : Mahatma Gandhi");
        script.setContent("In all probability this will be my last speech to you. Even if the Government allow me to march tomorrow morning, this will be my last speech on the sacred banks of the Sabarmati. Possibly these may be the last words of my life here.\n" +
                "\n" +
                "I have already told you yesterday what I had to say. Today I shall confine myself to what you should do after my companions and I are arrested. The programme of the march to Jalalpur must be fulfilled as originally settled. The enlistment of the volunteers for this purpose should be confined to Gujarat only. From what I have been and heard during the last fortnight, I am inclined to believe that the stream of civil resisters will flow unbroken.\n" +
                "\n" +
                "But let there be not a semblance of breach of peace even after all of us have been arrested. We have resolved to utilize all our resources in the pursuit of an exclusively nonviolent struggle. Let no one commit a wrong in anger. This is my hope and prayer. I wish these words of mine reached every nook and corner of the land.\n" +
                "\n" +
                "My task shall be done if I perish and so do my comrades. It will then be for the Working Committee of the Congress to show you the way and it will be up to you to follow its lead. So long as I have reached Jalalpur, let nothing be done in contravention to the authority vested in me by the Congress. But once I am arrested, the whole responsibility shifts to the Congress.\n" +
                "\n" +
                "No one who believes in non-violence, as a creed, need, therefore, sit still. My compact with the Congress ends as soon as I am arrested. In that case volunteers. Wherever possible, civil disobedience of salt should be started. These laws can be violated in three ways. It is an offence to manufacture salt wherever there are facilities for doing so.\n" +
                "\n" +
                "The possession and sale of contraband salt, which includes natural salt or salt earth, is also an offence. The purchasers of such salt will be equally guilty. To carry away the natural salt deposits on the seashore is likewise violation of law. So is the hawking of such salt. In short, you may  choose any one or all of these devices to break the salt monopoly.");
        scripts.add(script);

        return scripts;
    }
}
