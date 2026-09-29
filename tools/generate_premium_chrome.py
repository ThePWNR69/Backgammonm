from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageChops
import random, math

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'app/src/main/res/drawable-nodpi'
OUT.mkdir(parents=True, exist_ok=True)
S = 4
random.seed(1120)

BURG_TOP = (134, 25, 42, 255)
BURG_MID = (102, 13, 30, 255)
BURG_BOTTOM = (62, 6, 16, 255)
DARK = (24, 17, 15, 255)
DARK2 = (44, 31, 27, 255)
DARK3 = (58, 43, 38, 255)
GOLD = (197, 140, 59, 255)
GOLD_HI = (244, 203, 111, 255)
GOLD_SOFT = (224, 173, 82, 255)
GOLD_DARK = (102, 62, 24, 255)
CREAM = (255, 239, 202, 255)


def vgrad(size, top, bottom):
    w,h=size
    im=Image.new('RGBA', size)
    d=ImageDraw.Draw(im)
    for y in range(h):
        t=y/max(1,h-1)
        c=tuple(round(top[i]*(1-t)+bottom[i]*t) for i in range(4))
        d.line((0,y,w,y), fill=c)
    return im


def rounded_mask(size, radius):
    m=Image.new('L', size, 0)
    d=ImageDraw.Draw(m)
    d.rounded_rectangle((0,0,size[0]-1,size[1]-1), radius=radius, fill=255)
    return m


def grain_overlay(size, strength=18, blur=.35, opacity=.18):
    noise=Image.effect_noise(size, strength).convert('L').filter(ImageFilter.GaussianBlur(blur*S))
    a=noise.point(lambda v: int(abs(v-128)*opacity))
    ov=Image.new('RGBA', size, (255,255,255,0))
    ov.putalpha(a)
    return ov


def leather(size, top=BURG_TOP, bottom=BURG_BOTTOM, grain=18):
    base=vgrad(size, top, bottom)
    # fine grain + faint vertical tonal movement
    base=Image.alpha_composite(base, grain_overlay(size, grain, .28, .12))
    w,h=size
    sheen=Image.new('RGBA', size, (0,0,0,0)); d=ImageDraw.Draw(sheen)
    d.rectangle((0,0,w,int(h*.18)), fill=(255,236,207,18))
    d.rectangle((0,int(h*.72),w,h), fill=(15,0,6,24))
    sheen=sheen.filter(ImageFilter.GaussianBlur(.9*S))
    return Image.alpha_composite(base,sheen)


def add_shadow(im, radius, offset=(0,1.5), alpha=95):
    w,h=im.size
    sh=Image.new('RGBA',(w,h),(0,0,0,0)); d=ImageDraw.Draw(sh)
    ox,oy=offset
    d.rounded_rectangle((2*S+ox*S,2*S+oy*S,w-2*S+ox*S,h-2*S+oy*S), radius=radius*S, fill=(0,0,0,alpha))
    sh=sh.filter(ImageFilter.GaussianBlur(1.1*S))
    return Image.alpha_composite(sh,im)


def stitch_line(d, box, radius, color=(246,198,107,80), dash=3.3, gap=2.2, width=.32):
    # subtle straight stitches on top/bottom only; corners remain clean
    x0,y0,x1,y1=box
    step=(dash+gap)*S
    seg=dash*S
    w=max(1,int(width*S))
    x=x0+radius*S
    while x < x1-radius*S:
        d.line((x,y0,x+seg,y0), fill=color, width=w)
        d.line((x,y1,x+seg,y1), fill=color, width=w)
        x += step


def panel_base(wu,hu,r=5.5, leather_top=BURG_TOP, leather_bottom=BURG_BOTTOM, gold_width=.85, stitch=True):
    w,h=int(wu*S),int(hu*S)
    canvas=Image.new('RGBA',(w,h),(0,0,0,0))
    # outer soft shadow
    sh=Image.new('RGBA',(w,h),(0,0,0,0)); sd=ImageDraw.Draw(sh)
    sd.rounded_rectangle((1.8*S,2.4*S,w-1.2*S,h-.8*S), radius=r*S, fill=(0,0,0,105))
    sh=sh.filter(ImageFilter.GaussianBlur(1.25*S)); canvas.alpha_composite(sh)
    mask=rounded_mask((w,h),r*S)
    tex=leather((w,h),leather_top,leather_bottom,15)
    canvas=Image.composite(tex,canvas,mask)
    d=ImageDraw.Draw(canvas)
    # multi-layer metal rim: highlight -> body -> dark hairline
    d.rounded_rectangle((.7*S,.7*S,w-.7*S,h-.7*S), radius=r*S, outline=GOLD_DARK, width=max(2,int(.7*S)))
    d.rounded_rectangle((1.35*S,1.35*S,w-1.35*S,h-1.35*S), radius=(r-.45)*S, outline=GOLD_HI, width=max(2,int(gold_width*S)))
    d.rounded_rectangle((2.45*S,2.45*S,w-2.45*S,h-2.45*S), radius=(r-1.3)*S, outline=(92,48,24,220), width=max(1,int(.45*S)))
    d.line((4*S,3.6*S,w-4*S,3.6*S), fill=(255,233,165,75), width=max(1,int(.28*S)))
    d.line((4*S,h-3.2*S,w-4*S,h-3.2*S), fill=(25,0,8,130), width=max(1,int(.35*S)))
    if stitch:
        stitch_line(d,(4*S,4.6*S,w-4*S,h-4.6*S),3.5,color=(245,188,98,52),dash=2.2,gap=2.5,width=.25)
    return canvas


def rivet(d, cx, cy, r=.75):
    x,y=cx*S,cy*S; rr=r*S
    d.ellipse((x-rr,y-rr,x+rr,y+rr), fill=GOLD_DARK, outline=GOLD_HI, width=max(1,int(.25*S)))
    d.ellipse((x-.28*S,y-.36*S,x+.10*S,y+.02*S), fill=(255,233,170,120))


def player_panel(side):
    wu,hu=146,30
    im=panel_base(wu,hu,r=5.4,gold_width=.78,stitch=True)
    d=ImageDraw.Draw(im)
    w,h=im.size
    # inset score bay with faceted inward shoulder
    bay=29*S
    if side=='left':
        pts=[(w-bay-4*S,3.1*S),(w-3.2*S,3.1*S),(w-3.2*S,h-3.1*S),(w-bay-4*S,h-3.1*S),(w-bay-9.2*S,h/2)]
        seam_x=34*S
        rivets=((5.2,5.2),(5.2,24.8))
    else:
        pts=[(3.2*S,3.1*S),(bay+4*S,3.1*S),(bay+9.2*S,h/2),(bay+4*S,h-3.1*S),(3.2*S,h-3.1*S)]
        seam_x=w-34*S
        rivets=((140.8,5.2),(140.8,24.8))
    d.polygon(pts, fill=(24,17,15,246))
    d.line(pts+[pts[0]], fill=GOLD_DARK, width=max(1,int(.55*S)), joint='curve')
    # bright inner edge on score bay
    inset=[]
    cx=sum(p[0] for p in pts)/len(pts); cy=sum(p[1] for p in pts)/len(pts)
    for x,y in pts:
        inset.append((x+(cx-x)*.055,y+(cy-y)*.09))
    d.line(inset+[inset[0]], fill=(238,185,90,150), width=max(1,int(.35*S)), joint='curve')
    # identity seam and tiny decorative rivets
    d.line((seam_x,5.2*S,seam_x,h-5.2*S), fill=(226,166,77,105), width=max(1,int(.35*S)))
    d.line((seam_x+1*S,5.2*S,seam_x+1*S,h-5.2*S), fill=(39,5,11,95), width=max(1,int(.25*S)))
    for p in rivets: rivet(d,*p,.62)
    # subdued diagonal leather highlights for depth
    for x in range(44,112,15):
        xx=x*S
        d.line((xx,6*S,xx+5*S,10*S),fill=(255,226,180,16),width=max(1,int(.25*S)))
    return im


def status_panel():
    wu,hu=142,30; w,h=wu*S,hu*S
    im=Image.new('RGBA',(w,h),(0,0,0,0))
    pts=[(8*S,.7*S),(w-8*S,.7*S),(w-.7*S,h/2),(w-8*S,h-.7*S),(8*S,h-.7*S),(.7*S,h/2)]
    mask=Image.new('L',(w,h),0); md=ImageDraw.Draw(mask); md.polygon(pts,fill=255)
    metal=vgrad((w,h),(253,216,122,255),(177,112,35,255))
    # brushed metal streaks
    streak=Image.new('RGBA',(w,h),(0,0,0,0)); sd=ImageDraw.Draw(streak)
    for y in range(3*S,h-3*S,2*S):
        a=10 + (y//(2*S))%2*5
        sd.line((7*S,y,w-7*S,y),fill=(255,244,189,a),width=max(1,int(.25*S)))
    metal=Image.alpha_composite(metal,streak)
    im=Image.composite(metal,im,mask)
    d=ImageDraw.Draw(im)
    d.line(pts+[pts[0]],fill=(75,42,16,255),width=max(2,int(1.0*S)),joint='curve')
    inset=[(10*S,2.5*S),(w-10*S,2.5*S),(w-3.8*S,h/2),(w-10*S,h-2.5*S),(10*S,h-2.5*S),(3.8*S,h/2)]
    d.line(inset+[inset[0]],fill=GOLD_HI,width=max(1,int(.45*S)),joint='curve')
    # central faint plate creates more visual layering without changing text bounds
    plate=Image.new('RGBA',(w,h),(0,0,0,0)); pd=ImageDraw.Draw(plate)
    pd.rounded_rectangle((20*S,6*S,w-20*S,h-6*S),radius=3*S,fill=(255,238,177,18),outline=(92,52,20,60),width=max(1,int(.25*S)))
    im=Image.alpha_composite(im,plate); d=ImageDraw.Draw(im)
    # tiny burgundy jewels at the ends
    for cx in (11,wu-11):
        d.polygon([(cx*S,12*S),((cx+1.5)*S,15*S),(cx*S,18*S),((cx-1.5)*S,15*S)],fill=(105,18,30,210),outline=(255,215,121,150))
    return im


def square_button(state='normal'):
    wu=hu=30; w=h=wu*S
    shift=.8*S if state=='pressed' else 0
    top=(112,29,35,255) if state=='normal' else (79,10,21,255)
    bot=(58,13,17,255) if state=='normal' else (46,5,12,255)
    im=Image.new('RGBA',(w,h),(0,0,0,0))
    sh=Image.new('RGBA',(w,h),(0,0,0,0)); sd=ImageDraw.Draw(sh)
    sd.rounded_rectangle((2*S,2.7*S,w-1.5*S,h-1*S),radius=6*S,fill=(0,0,0,100)); sh=sh.filter(ImageFilter.GaussianBlur(1*S)); im.alpha_composite(sh)
    tex=leather((w,h),top,bot,12)
    m=rounded_mask((w,int(h-shift)),5.6*S)
    lay=Image.new('RGBA',(w,h),(0,0,0,0)); lay.paste(tex.crop((0,0,w,int(h-shift))),(0,int(shift)),m); im.alpha_composite(lay)
    d=ImageDraw.Draw(im); y0=shift+.7*S
    d.rounded_rectangle((.8*S,y0,w-.8*S,h-.8*S),radius=5.6*S,outline=GOLD_DARK,width=max(2,int(.7*S)))
    d.rounded_rectangle((1.6*S,y0+.8*S,w-1.6*S,h-1.6*S),radius=4.8*S,outline=GOLD_HI,width=max(2,int(.62*S)))
    d.rounded_rectangle((3*S,y0+2.2*S,w-3*S,h-3*S),radius=3.8*S,outline=(84,43,22,190),width=max(1,int(.35*S)))
    rivet(d,5.2,5.3+shift/S,.56); rivet(d,24.8,5.3+shift/S,.56)
    if state=='normal': d.line((6*S,4*S,w-6*S,4*S),fill=(255,235,170,70),width=max(1,int(.25*S)))
    else: d.line((5*S,y0+3*S,w-5*S,y0+3*S),fill=(18,0,6,110),width=max(1,int(.45*S)))
    return im


def bottom_deck():
    wu,hu=510,39; w,h=wu*S,hu*S
    im=panel_base(wu,hu,r=6.4,gold_width=.75,stitch=True)
    d=ImageDraw.Draw(im)
    # inner stitched leather deck line
    d.rounded_rectangle((3.3*S,3.3*S,w-3.3*S,h-3.3*S),radius=4.6*S,outline=(74,32,21,210),width=max(1,int(.4*S)))
    # subtle zones / rails so the bar feels engineered rather than flat
    d.line((124*S,6*S,124*S,h-6*S),fill=(228,171,78,80),width=max(1,int(.35*S)))
    d.line((125*S,6*S,125*S,h-6*S),fill=(35,4,10,85),width=max(1,int(.3*S)))
    d.line((354*S,6*S,354*S,h-6*S),fill=(228,171,78,80),width=max(1,int(.35*S)))
    d.line((355*S,6*S,355*S,h-6*S),fill=(35,4,10,85),width=max(1,int(.3*S)))
    # two recessed dice wells, now with metal frames and inner shadows
    for cx in (47.5,80.5):
        x0=int((cx-13.5)*S); y0=6*S; x1=int((cx+13.5)*S); y1=33*S
        d.rounded_rectangle((x0,y0,x1,y1),radius=5*S,fill=(27,10,13,205),outline=GOLD_DARK,width=max(2,int(.55*S)))
        d.rounded_rectangle((x0+1.2*S,y0+1.2*S,x1-1.2*S,y1-1.2*S),radius=4*S,outline=(218,161,72,135),width=max(1,int(.32*S)))
        d.line((x0+4*S,y0+2.2*S,x1-4*S,y0+2.2*S),fill=(255,223,145,45),width=max(1,int(.25*S)))
        rivet(d,cx-9.8,9.1,.48); rivet(d,cx+9.8,9.1,.48)
    # decorative end cap details
    for cx in (8.0,502.0):
        d.line((cx*S,10*S,cx*S,29*S),fill=(226,168,79,100),width=max(1,int(.35*S)))
        rivet(d,cx,7.1,.48); rivet(d,cx,31.9,.48)
    # central shallow trough behind primary action button
    d.rounded_rectangle((157*S,5.2*S,345*S,33.8*S),radius=6*S,outline=(227,169,78,55),width=max(1,int(.3*S)))
    return im


def action_button(wu,hu,kind='primary',state='normal'):
    w,h=wu*S,hu*S
    im=Image.new('RGBA',(w,h),(0,0,0,0))
    if kind=='primary':
        top=(143,30,47,255); bot=(82,7,21,255); rad=6.5
        if state=='pressed': top=(92,13,29,255); bot=(58,5,15,255)
        elif state=='disabled': top=(77,58,56,235); bot=(50,39,38,235)
    else:
        top=(78,62,55,255); bot=(45,36,33,255); rad=5.5
        if state=='pressed': top=(54,43,39,255); bot=(34,27,25,255)
        elif state=='disabled': top=(63,59,56,220); bot=(47,44,42,220)
    shift=.9*S if state=='pressed' else 0
    sh=Image.new('RGBA',(w,h),(0,0,0,0)); sd=ImageDraw.Draw(sh)
    sd.rounded_rectangle((2*S,2.4*S,w-1.4*S,h-.7*S),radius=rad*S,fill=(0,0,0,100)); sh=sh.filter(ImageFilter.GaussianBlur(.85*S)); im.alpha_composite(sh)
    tex=leather((w,h),top,bot,12)
    m=rounded_mask((w,int(h-shift)),rad*S)
    lay=Image.new('RGBA',(w,h),(0,0,0,0)); lay.paste(tex.crop((0,0,w,int(h-shift))),(0,int(shift)),m); im.alpha_composite(lay)
    d=ImageDraw.Draw(im); y0=shift+.7*S
    alpha=110 if state=='disabled' else 255
    d.rounded_rectangle((.8*S,y0,w-.8*S,h-.8*S),radius=rad*S,outline=(GOLD_DARK[0],GOLD_DARK[1],GOLD_DARK[2],alpha),width=max(2,int(.7*S)))
    d.rounded_rectangle((1.6*S,y0+.8*S,w-1.6*S,h-1.6*S),radius=(rad-.8)*S,outline=(GOLD_HI[0],GOLD_HI[1],GOLD_HI[2],alpha),width=max(2,int(.6*S)))
    d.rounded_rectangle((3.2*S,y0+2.4*S,w-3.2*S,h-3.1*S),radius=(rad-1.8)*S,outline=(77,40,24,150 if state!='disabled' else 60),width=max(1,int(.35*S)))
    if state=='normal':
        d.line((7*S,4*S,w-7*S,4*S),fill=(255,231,157,65),width=max(1,int(.28*S)))
        d.line((7*S,h-3.5*S,w-7*S,h-3.5*S),fill=(22,0,6,90),width=max(1,int(.28*S)))
    elif state=='pressed':
        d.line((6*S,y0+3*S,w-6*S,y0+3*S),fill=(15,0,5,125),width=max(1,int(.5*S)))
    # tiny corner studs on utility controls; primary gets centered ornamental ticks instead
    if kind=='secondary':
        rivet(d,5.2,5.2+shift/S,.45); rivet(d,wu-5.2,5.2+shift/S,.45)
    else:
        d.line((10*S,h/2,14*S,h/2),fill=(237,185,91,100),width=max(1,int(.35*S)))
        d.line((w-14*S,h/2,w-10*S,h/2),fill=(237,185,91,100),width=max(1,int(.35*S)))
    return im

assets={
    'premium_player_left.webp': player_panel('left'),
    'premium_player_right.webp': player_panel('right'),
    'premium_status.webp': status_panel(),
    'premium_bottom_deck.webp': bottom_deck(),
    'premium_primary_normal.webp': action_button(174,31,'primary','normal'),
    'premium_primary_pressed.webp': action_button(174,31,'primary','pressed'),
    'premium_primary_disabled.webp': action_button(174,31,'primary','disabled'),
    'premium_secondary_normal.webp': action_button(58,31,'secondary','normal'),
    'premium_secondary_pressed.webp': action_button(58,31,'secondary','pressed'),
    'premium_secondary_disabled.webp': action_button(58,31,'secondary','disabled'),
    'premium_square_normal.webp': square_button('normal'),
    'premium_square_pressed.webp': square_button('pressed'),
}
for name,img in assets.items():
    img.save(OUT/name,'WEBP',quality=97,method=6)
print('Generated',len(assets),'v1.12 detailed premium chrome assets in',OUT)
