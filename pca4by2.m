function [S] = pca4by2(batch1, batch2, batch15, batch16,batch40, batch56, batch78, batch99)

a=batch1';
b=batch2';
x=batch15';
y=batch16';

u=batch40';
v=batch56';
r=batch78';
s=batch99';

xy=[a;b;x;y;u;v;r;s];

mu=ones(1,size(xy,1))*xy;
xy-=(mu./size(xy,1));
mean(mu./size(xy,1));

sigma=xy'*xy;
sigma=sigma./size(xy,1);
[U,S,V]=svd(sigma);

Z1=a*U(:,1:2);
Z2=b*U(:,1:2);
Z3=x*U(:,1:2);
Z4=y*U(:,1:2);

Z5=u*U(:,1:2);
Z6=v*U(:,1:2);
Z7=r*U(:,1:2);
Z8=s*U(:,1:2);
Zall=[Z1;Z2;Z3;Z4;Z5;Z6;Z7;Z8];

palette=hsv(41);
colors=[ones(size(Z1,1),3).*palette(1,:);
	ones(size(Z2,1),3).*palette(4,:);
	ones(size(Z3,1),3).*palette(7,:);
	ones(size(Z4,1),3).*palette(10,:);
	ones(size(Z5,1),3).*palette(20,:);
	ones(size(Z6,1),3).*palette(23,:);
	ones(size(Z7,1),3).*palette(26,:);
	ones(size(Z8,1),3).*palette(29,:)];
scatter(Zall(:,1),Zall(:,2),8,colors);
