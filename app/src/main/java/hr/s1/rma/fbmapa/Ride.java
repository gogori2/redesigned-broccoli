package hr.s1.rma.fbmapa;

public class Ride {

    String start;
    String end;
    String vozac;
    Long status;
    String contact;
    String uid_vozaca;

    public Ride(String s, String e, String v, String c, Long ss) {
        start=s;
        end=e;
        vozac=v;
        status=ss;
        contact=c;
        // uid_vozaca=id;

    }

    @Override
    public String toString() {
        String ispis = start + " " + end;
        if (status == 2) ispis = ispis + "\n" + "vozi me: " + vozac;
        else ispis = ispis + "\n" + "vožnja nije potvrđena";
        return ispis;

    }

    public String getVozac() {
        return vozac;
    }

    public Long getStatus(){
        return status;
    }
}
