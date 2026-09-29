from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageChops
import random, math

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'app/src/main/res/drawable-nodpi'
OUT.mkdir(parents=True, exist_ok=True)
S = 4
random.seed(1110)

BURG_TOP = (126, 22, 37, 255)
BURG_BOTTOM = (74, 7, 18, 255)
BURG_MID = (101, 12, 27, 255)
DARK = (24, 18, 16, 255)
DARK2 = (46, 34, 29, 255)
GOLD = (201, 146, 65, 255)
GOLD_HI = (239, 195, 105, 255)
GOLD_DARK = (116, 73, 28, 255)
CREAM = (255, 239, 200, 255)


def vgrad(size, top, bottom):
    w,h=size
    im=Image.new('RGBA', size)
    px=im.load()
    for y in range(h):
        t=y/max(1,h-1)
        c=tuple(round(top[i]*(1-t)+bottom[i]*t) for i in range(4))
        for x in range(w): px[x,y]=c
    return im


def leather(size, top=BURG_TOP, bottom=BURG_BOTTOM, amount=8):
    base=vgrad(size, top, bottom)
    n=Image.new('L', size, 128)
    p=n.load(); w,h=size
    for y in range(h):
        for x in range(w):
            p[x,y]=max(0,min(255,128+random.randint(-amount,amount)))
    n=n.filter(ImageFilter.GaussianBlur(0.35*S))
    tint=Image.new('RGBA', size, (18,7,4,0))
    # subtle monochrome texture, very low opacity
    a=n.point(lambda v: int(abs(v-128)*0.34))
    tint.putalpha(a)
    return Image.alpha_composite(base,tint)


def rounded_mask(size, radius):
    m=Image.new('L', size, 0); d=ImageDraw.Draw(m)
    d.rounded_rectangle((0,0,size[0]-1,size[1]-1), radius=radius, fill=255)
    return m


def composite_masked(dst, src, mask):
    dst.alpha_composite(Image.composite(src, Image.new('RGBA', src.size, (0,0,0,0)), mask))


def panel_base(wu,hu,r=6, gold_width=1.3, inner=True):
    w,h=int(wu*S),int(hu*S)
    im=Image.new('RGBA',(w,h),(0,0,0,0))
    shadow=Image.new('RGBA',(w,h),(0,0,0,0)); sd=ImageDraw.Draw(shadow)
    sd.rounded_rectangle((2*S,2*S,w-1, h-1), radius=r*S, fill=(0,0,0,95))
    shadow=shadow.filter(ImageFilter.GaussianBlur(1.2*S)); im.alpha_composite(shadow)
    mask=rounded_mask((w,h),r*S)
    tex=leather((w,h))
    im=Image.composite(tex,im,mask)
    d=ImageDraw.Draw(im)
    gw=max(2,int(gold_width*S))
    d.rounded_rectangle((1*S,1*S,w-1*S,h-1*S), radius=r*S, outline=GOLD_HI, width=gw)
    d.rounded_rectangle((2.3*S,2.3*S,w-2.3*S,h-2.3*S), radius=(r-1.3)*S, outline=GOLD_DARK, width=max(1,int(.7*S)))
    if inner:
        d.line((4*S,4*S,w-4*S,4*S), fill=(255,226,146,80), width=max(1,S//2))
        d.line((4*S,h-4*S,w-4*S,h-4*S), fill=(31,2,7,110), width=max(1,S//2))
    return im


def player_panel(side):
    wu,hu=146,30
    im=panel_base(wu,hu,r=6,gold_width=1.05)
    d=ImageDraw.Draw(im)
    w,h=im.size
    bay_w=26*S
    if side=='left':
        pts=[(w-bay_w-5*S,3*S),(w-3*S,3*S),(w-3*S,h-3*S),(w-bay_w-5*S,h-3*S),(w-bay_w-10*S,h//2)]
    else:
        pts=[(3*S,3*S),(bay_w+5*S,3*S),(bay_w+10*S,h//2),(bay_w+5*S,h-3*S),(3*S,h-3*S)]
    d.polygon(pts, fill=(24,18,16,232))
    d.line(pts+[pts[0]], fill=GOLD_HI, width=max(2,int(.9*S)), joint='curve')
    # very subtle vertical separation near identity area
    if side=='left':
        x=32*S
    else:
        x=w-32*S
    d.line((x,5*S,x,h-5*S), fill=(228,171,82,80), width=max(1,int(.45*S)))
    return im


def status_panel():
    wu,hu=140,30; w,h=wu*S,hu*S
    im=Image.new('RGBA',(w,h),(0,0,0,0))
    # faceted plaque, slimmer than prior version
    pts=[(8*S,1*S),(w-8*S,1*S),(w-1*S,h//2),(w-8*S,h-1*S),(8*S,h-1*S),(1*S,h//2)]
    mask=Image.new('L',(w,h),0); md=ImageDraw.Draw(mask); md.polygon(pts,fill=255)
    gold=vgrad((w,h),(247,205,104,255),(180,118,40,255))
    noise=Image.effect_noise((w,h),16).convert('L').filter(ImageFilter.GaussianBlur(.35*S))
    overlay=Image.new('RGBA',(w,h),(255,255,255,0)); overlay.putalpha(noise.point(lambda v: int(abs(v-128)*.10)))
    gold=Image.alpha_composite(gold,overlay)
    im=Image.composite(gold,im,mask)
    d=ImageDraw.Draw(im)
    d.line(pts+[pts[0]],fill=GOLD_DARK,width=max(2,int(1.25*S)),joint='curve')
    inset=[(10*S,3*S),(w-10*S,3*S),(w-4*S,h//2),(w-10*S,h-3*S),(10*S,h-3*S),(4*S,h//2)]
    d.line(inset+[inset[0]],fill=(255,226,139,175),width=max(1,int(.55*S)),joint='curve')
    return im


def square_button(state='normal'):
    wu=hu=30; w=h=wu*S
    shift = 1*S if state=='pressed' else 0
    im=Image.new('RGBA',(w,h),(0,0,0,0))
    if state=='pressed':
        top=(82,11,22,255); bot=(55,6,13,255)
    else:
        top=(113,31,34,255); bot=(64,18,20,255)
    tex=leather((w,h),top,bot,5)
    mask=rounded_mask((w,h-shift),6*S)
    tmp=Image.new('RGBA',(w,h),(0,0,0,0)); tmp.paste(tex.crop((0,0,w,h-shift)),(0,shift),mask)
    im.alpha_composite(tmp)
    d=ImageDraw.Draw(im)
    y0=shift+1*S
    d.rounded_rectangle((1*S,y0,w-1*S,h-1*S),radius=6*S,outline=GOLD_HI,width=max(2,int(1.0*S)))
    d.rounded_rectangle((2.4*S,y0+1.4*S,w-2.4*S,h-2.4*S),radius=4.5*S,outline=GOLD_DARK,width=max(1,int(.65*S)))
    if state=='normal': d.line((5*S,4*S,w-5*S,4*S),fill=(255,226,150,80),width=1*S)
    return im


def bottom_deck():
    wu,hu=510,39; w,h=wu*S,hu*S
    im=panel_base(wu,hu,r=7,gold_width=1.0)
    d=ImageDraw.Draw(im)
    # quieter inner border and two recessed dice wells.
    d.rounded_rectangle((3*S,3*S,w-3*S,h-3*S),radius=5*S,outline=(92,48,22,180),width=max(1,int(.5*S)))
    for cx in (47.5,80.5):
        x0=int((cx-13.5)*S); y0=6*S; x1=int((cx+13.5)*S); y1=33*S
        d.rounded_rectangle((x0,y0,x1,y1),radius=5*S,fill=(38,16,18,150),outline=(144,88,37,150),width=max(1,int(.55*S)))
        d.line((x0+4*S,y0+2*S,x1-4*S,y0+2*S),fill=(236,184,91,42),width=1*S)
    return im


def action_button(wu,hu,kind='primary',state='normal'):
    w,h=wu*S,hu*S
    im=Image.new('RGBA',(w,h),(0,0,0,0))
    if kind=='primary':
        if state=='pressed': top=(102,14,29,255); bot=(61,6,15,255)
        elif state=='disabled': top=(79,58,55,235); bot=(52,40,38,235)
        else: top=(139,27,43,255); bot=(91,9,23,255)
        rad=7
    else:
        if state=='pressed': top=(59,48,43,255); bot=(38,31,29,255)
        elif state=='disabled': top=(65,61,58,220); bot=(48,45,43,220)
        else: top=(81,65,57,255); bot=(52,42,38,255)
        rad=6
    shift=1*S if state=='pressed' else 0
    tex=leather((w,h),top,bot,4)
    mask=rounded_mask((w,h-shift),rad*S)
    tmp=Image.new('RGBA',(w,h),(0,0,0,0)); tmp.paste(tex.crop((0,0,w,h-shift)),(0,shift),mask)
    im.alpha_composite(tmp)
    d=ImageDraw.Draw(im)
    alpha=120 if state=='disabled' else 255
    y0=shift+1*S
    d.rounded_rectangle((1*S,y0,w-1*S,h-1*S),radius=rad*S,outline=(GOLD_HI[0],GOLD_HI[1],GOLD_HI[2],alpha),width=max(2,int(.95*S)))
    d.rounded_rectangle((2.4*S,y0+1.4*S,w-2.4*S,h-2.4*S),radius=(rad-1.4)*S,outline=(GOLD_DARK[0],GOLD_DARK[1],GOLD_DARK[2],alpha),width=max(1,int(.6*S)))
    if state=='normal':
        d.line((6*S,4*S,w-6*S,4*S),fill=(255,229,159,62),width=max(1,S//2))
    if state=='pressed':
        d.line((5*S,y0+3*S,w-5*S,y0+3*S),fill=(18,3,7,100),width=1*S)
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
    img.save(OUT/name,'WEBP',quality=96,method=6)
print('Generated',len(assets),'premium chrome assets in',OUT)
