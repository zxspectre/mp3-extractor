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

names = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
x_vals = {Z1(:,1), Z2(:,1), Z3(:,1), Z4(:,1), Z5(:,1), Z6(:,1), Z7(:,1), Z8(:,1)};
y_vals = {Z1(:,2), Z2(:,2), Z3(:,2), Z4(:,2), Z5(:,2), Z6(:,2), Z7(:,2), Z8(:,2)};
sizes  = {10, 10, 10, 10, 10, 10, 10, 10};
colors = {hsv(1), hsv(2), hsv(3), hsv(4), hsv(5), hsv(6), hsv(7), hsv(8)};
styles = {'s', 's', 's', 's', 's', 's', 's', 's'}

scatter_series_set(x_vals, y_vals, sizes, colors, styles);
legend(names, 'location', 'southeast');