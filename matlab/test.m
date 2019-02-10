
a = gendatmilg([5 5]);
b = a;
b(3:10,:)=[];
c = addlabels(a,ones(size(a,1),1),'nothing');
c = changelablist(c,'nothing');

ismilset(a);
hasmilbags(a);
ismillabeled(a);
ispositive(a);
ispositive(getlab(a));
[x,lab,bagid,xI] = getbags(a);
a7 = a(xI{7},:);
nlab = [1; 1; 0; 0; 0; 0];
labelset(nlab,'first');
labelset(nlab,'majority');
labelset(nlab,'presence');
labelset(nlab,1);
labelset(nlab,3);
labelset(nlab,0.5);

[bp,bn,Ip,In]=getpositivebags(b);

[w,u] = miles(a);


